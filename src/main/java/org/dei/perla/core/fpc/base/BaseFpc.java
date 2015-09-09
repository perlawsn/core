package org.dei.perla.core.fpc.base;

import org.dei.perla.core.fpc.Fpc;
import org.dei.perla.core.fpc.Task;
import org.dei.perla.core.fpc.TaskHandler;
import org.dei.perla.core.fpc.Attribute;
import org.dei.perla.core.fpc.SamplePipeline;
import org.dei.perla.core.utils.AsyncUtils;
import org.dei.perla.core.utils.Check;

import java.util.*;
import java.util.function.Consumer;

/**
 * Base implementation of the {@link Fpc} interface.
 *
 * @author Guido Rota (2014)
 */
public final class BaseFpc implements Fpc {

    private final int id;
    private final String type;
    private final Set<Attribute> atts;
    private final Map<Attribute, Object> staticAtts;
    private final ChannelManager cmgr;
    private final Scheduler sched;

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
    public Task get(List<Attribute> requestAtts, boolean strict,
            TaskHandler handler) {
        if (Check.nullOrEmpty(atts)) {
            throw new RuntimeException(
                    "Cannot sample, attribute list is null or empty");
        }
        Request req = new Request(requestAtts, staticAtts);

        if (req.isStatic()) {
            Task t = new CompletedTask(req.getStatic());
            // Running in a new thread to preserve asynchronous semantics
            AsyncUtils.runInNewThread(() -> {
                handler.data(t, req.newStaticSample());
                handler.complete(t);
            });
            return t;
        } else {
            Operation op = sched.get(req.getDynamic(), strict);
            if (op == null) {
                return null;
            }

            SamplePipeline pipe = req.createPipeline(op.getAttributes());
            BaseTask t = op.schedule(Collections.emptyMap(), handler, pipe);
            t.start();
            return t;
        }
    }

    @Override
    public Task get(List<Attribute> requestAtts, boolean strict, long ms,
            TaskHandler handler) {
        if (Check.nullOrEmpty(atts)) {
            throw new RuntimeException(
                    "Cannot sample, attribute list is null or empty");
        }
        Request req = new Request(requestAtts, staticAtts);

        if (req.isStatic()) {
            StaticPeriodicTask t = new StaticPeriodicTask(req, ms, handler);
            t.start();
            return t;

        } else {
            Operation op = sched.periodic(req.getDynamic(), strict);
            if (op == null) {
                return null;
            }

            Map<String, Object> pm = new HashMap<>();
            pm.put("period", ms);

            SamplePipeline pipe = req.createPipeline(op.getAttributes());
            BaseTask t = op.schedule(pm, handler, pipe);
            t.start();
            return t;
        }
    }

    @Override
    public Task async(List<Attribute> requestAtts, boolean strict,
            TaskHandler handler) {
        if (Check.nullOrEmpty(atts)) {
            throw new RuntimeException(
                    "Cannot sample, attribute list is null or empty");
        }
        Request req = new Request(requestAtts, staticAtts);

        if (req.isStatic()) {
            return null;
        }

        Operation op = sched.async(req.getDynamic(), strict);
        if (op == null) {
            return null;
        }
        SamplePipeline pipe = req.createPipeline(op.getAttributes());
        BaseTask t = op.schedule(Collections.emptyMap(), handler, pipe);
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
