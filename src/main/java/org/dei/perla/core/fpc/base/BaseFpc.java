package org.dei.perla.core.fpc.base;

import org.dei.perla.core.engine.EmitInstruction;
import org.dei.perla.core.engine.Instruction;
import org.dei.perla.core.engine.Script;
import org.dei.perla.core.engine.StopInstruction;
import org.dei.perla.core.fpc.Fpc;
import org.dei.perla.core.fpc.Task;
import org.dei.perla.core.fpc.TaskHandler;
import org.dei.perla.core.sample.Attribute;
import org.dei.perla.core.sample.Sample;
import org.dei.perla.core.sample.SamplePipeline;
import org.dei.perla.core.sample.SamplePipeline.PipelineBuilder;
import org.dei.perla.core.utils.AsyncUtils;

import java.time.Instant;
import java.util.*;
import java.util.function.Consumer;

public class BaseFpc implements Fpc {

    private final int id;
    private final String type;
    private final Set<Attribute> atts;
    private final Map<Attribute, Object> staticAtts;
    private final ChannelManager cmgr;
    private final Scheduler sched;

    // This Operation creates an empty sample. It is used for scheduling the
    // periodic creation of empty samples, to which additional static fields
    // may be appended, to satisfy a periodic request of static attributes
    private static final Operation emptySampleOperation;

    static {
        Instruction start = new EmitInstruction();
        start.setNext(new StopInstruction());
        Script empty = new Script("_empty", start, Collections.emptyList(),
                Collections.emptyList());
        emptySampleOperation = new SimulatedPeriodicOperation("_empty", empty);
    }

    protected BaseFpc(int id, String type, Set<Attribute> atts,
            Map<Attribute, Object> staticAtts, ChannelManager cmgr,
            Scheduler sched) {
        this.id = id;
        this.type = type;
        this.atts = atts;
        this.staticAtts = staticAtts;
        this.cmgr = cmgr;
        this.sched = sched;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public Set<Attribute> getAttributes() {
        return atts;
    }

    protected Scheduler getOperationScheduler() {
        return sched;
    }

    @Override
    public Task set(Map<Attribute, Object> values, boolean strict,
            TaskHandler handler) {
        Operation op = sched.set(values.keySet(), strict);
        if (op == null) {
            return null;
        }

        Map<String, Object> pm = new HashMap<>();
        values.entrySet().forEach(
                e -> pm.put(e.getKey().getId(), e.getValue()));

        BaseTask t = op.schedule(pm, handler);
        t.start();
        return t;
    }

    @Override
    public Task get(List<Attribute> atts, boolean strict,
            TaskHandler handler) {
        Request req = new Request(atts);

        if (req.staticOnly()) {
            Task t = new CompletedTask(req.statAtts);
            // Running in a new thread to preserve asynchronous semantics
            AsyncUtils.runInNewThread(() -> {
                handler.data(t, req.staticSample());
                handler.complete(t);
            });
            return t;
        }

        Operation op = sched.get(req.dynAtts, strict);
        if (op == null) {
            return null;
        }

        PipelineBuilder pb = SamplePipeline.newBuilder(op.getAttributes());
        if (req.mixed()) {
            pb.addStatic(req.staticValues());
        }
        if (!op.getAttributes().contains(Attribute.TIMESTAMP)) {
            pb.addTimestamp();
        }
        pb.reorder(atts);

        BaseTask t = op.schedule(Collections.emptyMap(), handler, pb.create());
        t.start();
        return t;
    }

    @Override
    public Task get(List<Attribute> atts, boolean strict, long ms,
            TaskHandler handler) {
        PipelineBuilder pb;
        Request req = new Request(atts);

        if (req.staticOnly()) {
            pb = SamplePipeline.newBuilder(Collections.emptyList());
            pb.addStatic(req.staticValues());
            pb.addTimestamp();
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("period", ms);
            pb.reorder(atts);
            BaseTask t = emptySampleOperation
                    .schedule(paramMap, handler, pb.create());

            t.start();
            return t;
        }

        Operation op = sched.periodic(req.dynAtts, strict);
        if (op == null) {
            return null;
        }

        Map<String, Object> pm = new HashMap<>();
        pm.put("period", ms);

        pb = SamplePipeline.newBuilder(op.getAttributes());
        if (!req.statAtts.isEmpty()) {
            pb.addStatic(req.staticValues());
        }
        if (!op.getAttributes().contains(Attribute.TIMESTAMP)) {
            pb.addTimestamp();
        }
        pb.reorder(atts);

        BaseTask t = op.schedule(pm, handler, pb.create());
        t.start();
        return t;
    }

    @Override
    public Task async(List<Attribute> atts, boolean strict,
            TaskHandler handler) {
        Request req = new Request(atts);

        if (!req.statAtts.isEmpty()) {
            return null;
        }

        Operation op = sched.async(atts, strict);
        if (op == null) {
            return null;
        }

        PipelineBuilder pb = SamplePipeline.newBuilder(op.getAttributes());
        if (!op.getAttributes().contains(Attribute.TIMESTAMP)) {
            pb.addTimestamp();
        }

        BaseTask t = op.schedule(Collections.emptyMap(), handler, pb.create());
        t.start();
        return t;
    }

    @Override
    public void stop(final Consumer<Fpc> handler) {
        sched.stop((Void) -> {
            cmgr.stop();
            handler.accept(this);
        });
    }

    /**
     * Request is a simple utility class employed to classify the
     * attributes requested by the user as static or dynamic.
     */
    private class Request {

        private final List<Attribute> dynAtts = new ArrayList<>();
        private final List<Attribute> statAtts = new ArrayList<>();

        private Request(Collection<Attribute> atts) {
            atts.forEach(a -> {
                if (staticAtts.containsKey(a)) {
                    statAtts.add(a);
                } else {
                    dynAtts.add(a);
                }
            });
        }

        /**
         * staticOnly returns true if the request contains only static
         * attributes.
         */
        private boolean staticOnly() {
            return dynAtts.isEmpty() && !statAtts.isEmpty();
        }

        /**
         * mixed returns true if the request contains both static and dynamic
         * attributes.
         */
        private boolean mixed() {
            return !dynAtts.isEmpty() && !statAtts.isEmpty();
        }

        /**
         * staticSample returns a new sample composed only of static values.
         */
        private Sample staticSample() {
            Object[] values = new Object[atts.size() + 1];
            int i = 0;
            for (Attribute a : statAtts) {
                values[i] = staticAtts.get(a);
                i++;
            }
            statAtts.add(Attribute.TIMESTAMP);
            values[i] = Instant.now();
            return new Sample(Collections.unmodifiableList(statAtts), values);
        }

        private LinkedHashMap<Attribute, Object> staticValues() {
            LinkedHashMap<Attribute, Object> av = new LinkedHashMap<>();
            statAtts.forEach(a -> av.put(a, staticAtts.get(a)));
            return av;
        }

    }

    /**
     * Implementation of a {@link Task} that terminates immediately. This class is
     * mainly used as a return value for 'get' requests that only return static
     * attributes, or for which the result was already available.
     *
     * @author Guido Rota (2014)
     *
     */
    private class CompletedTask implements Task {

        private final List<Attribute> atts;

        public CompletedTask(List<Attribute> atts) {
            this.atts = atts;
        }

        @Override
        public boolean isRunning() {
            return false;
        }

        @Override
        public List<Attribute> getAttributes() {
            return atts;
        }

        @Override
        public void stop() {
        }

    }

}
