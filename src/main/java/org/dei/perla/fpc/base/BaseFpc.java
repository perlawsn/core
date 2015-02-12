package org.dei.perla.fpc.base;

import org.dei.perla.fpc.Attribute;
import org.dei.perla.fpc.Fpc;
import org.dei.perla.fpc.Task;
import org.dei.perla.fpc.TaskHandler;
import org.dei.perla.fpc.base.RecordPipeline.PipelineBuilder;
import org.dei.perla.fpc.engine.*;
import org.dei.perla.utils.StopHandler;

import java.time.ZonedDateTime;
import java.util.*;

public class BaseFpc implements Fpc {

	private final int id;
    private final String type;
	private final Set<Attribute> atts;
    private final Map<Attribute, Object> attValues;
	private final ChannelManager channelMgr;
	private final OperationScheduler scheduler;

	// This Operation creates an empty record. It is used for scheduling the
	// periodic creation of empty records, to which additional static fields
	// may be appended, to satisfy a periodic request of static attributes
	private static final Operation emptyRecordOperation;

	static {
		Script doNothing = ScriptBuilder.newScript().add(new EmitInstruction())
				.add(new StopInstruction()).buildScript("empty");
		emptyRecordOperation = new SimulatedPeriodicOperation("_empty",
				Collections.emptySet(), doNothing);
	}

	protected BaseFpc(int id, String type, Set<Attribute> atts,
            Map<Attribute, Object> attValues, ChannelManager channelMgr,
			OperationScheduler scheduler) {
		this.id = id;
        this.type = type;
        this.atts = atts;
        this.attValues = attValues;
        this.channelMgr = channelMgr;
		this.scheduler = scheduler;
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

	protected OperationScheduler getOperationScheduler() {
		return scheduler;
	}

	@Override
	public Task set(Map<Attribute, Object> valueMap, TaskHandler handler) {
		return scheduler.scheduleSet(valueMap, handler);
	}

	@Override
	public Task get(Collection<Attribute> atts, TaskHandler handler) {
		PipelineBuilder pBuilder = RecordPipeline.newBuilder();
        Request req = new Request(atts);

		if (req.staticOnly()) {
            Task t = new CompletedTask(req.statAtts);
            handler.newRecord(t, req.staticRecord());
            handler.complete(t);
            return t;

		} else if (req.mixed()) {
			pBuilder.add(new RecordModifier.StaticAppender(req.staticValues()));
		}

		return scheduler.scheduleGet(req.dynAtts, handler, pBuilder);
	}

	@Override
	public Task get(Collection<Attribute> atts, long periodMs,
			TaskHandler handler) {
		PipelineBuilder pBuilder = RecordPipeline.newBuilder();
        Request req = new Request(atts);

		if (!req.statAtts.isEmpty()) {
			pBuilder.add(new RecordModifier.StaticAppender(req.staticValues()));
		}

        if (req.staticOnly()) {
            pBuilder.add(new RecordModifier.TimestampAppender());
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("period", periodMs);
            return emptyRecordOperation.schedule(paramMap, handler,
                    pBuilder.create());
        } else {
            return scheduler.schedulePeriodic(req.dynAtts, periodMs, handler,
                    pBuilder);
        }
	}

	@Override
	public Task async(Collection<Attribute> atts, TaskHandler handler) {
		PipelineBuilder pBuilder = RecordPipeline.newBuilder();
        Request req = new Request(atts);

		if (!req.statAtts.isEmpty()) {
			return null;
		}

		return scheduler.scheduleAsync(req.dynAtts, handler, pBuilder);
	}

	@Override
	public void stop(final StopHandler<Fpc> handler) {
		scheduler.stop((Void) -> {
			channelMgr.stop();
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
         * dynamicOnly returns true if the request contains only dynamic
         * attributes.
         */
        private boolean dynamicOnly() {
            return !dynAtts.isEmpty() && statAtts.isEmpty();
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
            Map<String, Object> av = new HashMap<>();
            statAtts.forEach(a -> av.put(a.getId(), attValues.get(a)));
            av.put(Attribute.TIMESTAMP_ATTRIBUTE.getId(), ZonedDateTime.now());
            return Record.from(av);
        }

        private Map<Attribute, Object> staticValues() {
            Map<Attribute, Object> av = new HashMap<>();
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

		private final Collection<? extends Attribute> attributes;

		public CompletedTask(Collection<? extends Attribute> attributes) {
			this.attributes = attributes;
		}

		@Override
		public boolean isRunning() {
			return false;
		}

		@Override
		public Collection<? extends Attribute> getAttributes() {
			return attributes;
		}

		@Override
		public void stop() {
		}

	}

}
