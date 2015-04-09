package org.dei.perla.core.fpc.base;

import org.dei.perla.core.engine.EmitInstruction;
import org.dei.perla.core.engine.Instruction;
import org.dei.perla.core.engine.Script;
import org.dei.perla.core.engine.StopInstruction;
import org.dei.perla.core.fpc.Fpc;
import org.dei.perla.core.fpc.Task;
import org.dei.perla.core.fpc.TaskHandler;
import org.dei.perla.core.record.Attribute;
import org.dei.perla.core.record.Record;
import org.dei.perla.core.record.SamplePipeline;
import org.dei.perla.core.record.SamplePipeline.PipelineBuilder;
import org.dei.perla.core.utils.StopHandler;

import java.time.Instant;
import java.util.*;

public class BaseFpc implements Fpc {

	private final int id;
    private final String type;
	private final Set<Attribute> atts;
    private final Map<Attribute, Object> attValues;
	private final ChannelManager cmgr;
	private final Scheduler sched;

	// This Operation creates an empty record. It is used for scheduling the
	// periodic creation of empty records, to which additional static fields
	// may be appended, to satisfy a periodic request of static attributes
	private static final Operation emptyRecordOperation;

	static {
        Instruction start = new EmitInstruction();
        start.setNext(new StopInstruction());
        Script empty = new Script("_empty", start, Collections.emptyList(),
                Collections.emptyList());
		emptyRecordOperation = new SimulatedPeriodicOperation("_empty", empty);
	}

	protected BaseFpc(int id, String type, Set<Attribute> atts,
            Map<Attribute, Object> attValues, ChannelManager cmgr,
			Scheduler sched) {
		this.id = id;
        this.type = type;
        this.atts = atts;
        this.attValues = attValues;
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
		return op.schedule(pm, handler);
	}

	@Override
	public Task get(List<Attribute> atts, boolean strict,
			TaskHandler handler) {
        Request req = new Request(atts);

		if (req.staticOnly()) {
            Task t = new CompletedTask(req.statAtts);
            handler.newRecord(t, req.staticRecord());
            handler.complete(t);
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

		return op.schedule(Collections.emptyMap(), handler, pb.create());
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
			return emptyRecordOperation.schedule(paramMap, handler,
					pb.create());
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
		return op.schedule(pm, handler, pb.create());
	}

	@Override
	public Task async(List<Attribute> atts, boolean strict,
			TaskHandler handler) {
        Request req = new Request(atts);

		if (!req.statAtts.isEmpty()) {
			return null;
		}

		Operation op = sched.async(atts, strict);
		PipelineBuilder pb = SamplePipeline.newBuilder(op.getAttributes());
		if (op == null) {
			return null;
		}

		if (!op.getAttributes().contains(Attribute.TIMESTAMP)) {
			pb.addTimestamp();
		}
		return op.schedule(Collections.emptyMap(), handler, pb.create());
	}

	@Override
	public void stop(final StopHandler<Fpc> handler) {
		sched.stop((Void) -> {
			cmgr.stop();
			handler.hasStopped(this);
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
                if (attValues.containsKey(a)) {
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
         * staticRecord returns a new Record composed only of static values.
         */
        private Record staticRecord() {
            Object[] values = new Object[atts.size() + 1];
            int i = 0;
            for (Attribute a : statAtts) {
                values[i] = attValues.get(a);
                i++;
            }
            statAtts.add(Attribute.TIMESTAMP);
            values[i] = Instant.now();
            return new Record(Collections.unmodifiableList(statAtts), values);
        }

        private LinkedHashMap<Attribute, Object> staticValues() {
            LinkedHashMap<Attribute, Object> av = new LinkedHashMap<>();
            statAtts.forEach(a -> av.put(a, attValues.get(a)));
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
