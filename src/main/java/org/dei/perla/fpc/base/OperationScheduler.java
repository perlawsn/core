package org.dei.perla.fpc.base;

import org.dei.perla.fpc.Attribute;
import org.dei.perla.fpc.TaskHandler;
import org.dei.perla.fpc.base.RecordPipeline.PipelineBuilder;
import org.dei.perla.fpc.descriptor.DataType;
import org.dei.perla.utils.StopHandler;

import java.util.*;

public class OperationScheduler {

	// Used to order operations by number of attributes
	private static final Comparator<Operation> attComp =
            (Operation o1, Operation o2) -> {
        int o1s = o1.getAttributes().size();
        int o2s = o2.getAttributes().size();

        if (o1s < o2s) {
            return -1;
        } else if (o1s > o2s) {
            return 1;
        } else {
            return 0;
        }
    };

	private volatile boolean schedulable = true;

	private final List<? extends Operation> getOperationList;
	private final List<? extends Operation> setOperationList;
	private final List<? extends Operation> periodicOperationList;
	private final List<? extends Operation> asyncOperationList;

	public OperationScheduler(List<? extends Operation> getOperationList,
			List<? extends Operation> setOperationList,
			List<? extends Operation> periodicOperationList,
			List<? extends Operation> asyncOperationList) {

		this.getOperationList = getOperationList;
		this.setOperationList = setOperationList;
		this.periodicOperationList = periodicOperationList;
		this.asyncOperationList = asyncOperationList;

		Collections.sort(this.getOperationList, attComp);
		Collections.sort(this.setOperationList, attComp);
		Collections.sort(this.periodicOperationList, attComp);
		Collections.sort(this.asyncOperationList, attComp);
	}

	private RecordPipeline createTimestampedPipeline(Operation op,
			PipelineBuilder pBuilder) {
		if (op.getAttributes().contains(Attribute.TIMESTAMP_ATTRIBUTE)) {
			return pBuilder.create();
		}

		pBuilder.add(new RecordModifier.TimestampAppender());
		return pBuilder.create();
	}

	protected AbstractTask scheduleSet(Map<Attribute, Object> valueMap,
			TaskHandler handler) throws IllegalStateException {
		if (!schedulable) {
			throw new IllegalStateException("Scheduler has been stopped.");
		}

		Operation op = bestFit(setOperationList, valueMap.keySet());
		if (op == null) {
			return null;
		}

		Map<String, Object> paramMap = new HashMap<>();
		valueMap.entrySet().forEach(
				e -> paramMap.put(e.getKey().getId(), e.getValue()));
		return op.schedule(paramMap, handler);
	}

	protected AbstractTask scheduleGet(Collection<Attribute> attributes,
			TaskHandler handler, PipelineBuilder pBuilder)
			throws IllegalStateException {
		if (!schedulable) {
			throw new IllegalStateException("Scheduler has been stopped.");
		}

		Operation op = bestFit(getOperationList, attributes);
		if (op == null) {
			return null;
		}

		RecordPipeline pipeline = createTimestampedPipeline(op, pBuilder);
		return op.schedule(Collections.emptyMap(), handler, pipeline);
	}

	protected AbstractTask schedulePeriodic(
			Collection<Attribute> attributeList, long periodMs,
			TaskHandler handler, PipelineBuilder pBuilder)
			throws IllegalStateException {
		if (!schedulable) {
			throw new IllegalStateException("Scheduler has been stopped.");
		}

		Operation op = bestFit(periodicOperationList, attributeList);
		if (op == null) {
			return null;
		}

		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("period", periodMs);

		RecordPipeline pipeline = createTimestampedPipeline(op, pBuilder);
		return op.schedule(paramMap, handler, pipeline);
	}

	protected AbstractTask scheduleAsync(Collection<Attribute> attributes,
			TaskHandler handler, PipelineBuilder pBuilder)
			throws IllegalStateException {
		if (!schedulable) {
			throw new IllegalStateException("Scheduler has been stopped.");
		}

		Operation op = bestFit(asyncOperationList, attributes);
		if (op == null) {
			return null;
		}

		RecordPipeline pipeline = createTimestampedPipeline(op, pBuilder);
		return op.schedule(Collections.emptyMap(), handler, pipeline);
	}

	private Operation bestFit(List<? extends Operation> operationList,
			Collection<Attribute> attributes) {
		for (Operation op : operationList) {
			if (!hasAttributes(op, attributes)) {
				continue;
			}
			return op;
		}
		return null;
	}

	private boolean hasAttributes(Operation operation,
			Collection<Attribute> attributes) {
		Collection<Attribute> opAttSet = operation.getAttributes();

		for (Attribute att : attributes) {
			// Ignore timestamp attribute, since it will be added later if the
			// operation does not provide it natively (see method
			// createTimestampedPipeline)
			if (att.getId().compareToIgnoreCase(
					Attribute.TIMESTAMP_ATTRIBUTE.getId()) == 0
					&& att.getType() == DataType.TIMESTAMP) {
				continue;
			}
			if (!opAttSet.contains(att)) {
				return false;
			}
		}

		return true;
	}

	protected Operation getGetOperation(String id) {
		return findById(getOperationList, id);
	}

	protected Operation getSetOperation(String id) {
		return findById(setOperationList, id);
	}

	protected Operation getPeriodicOperation(String id) {
		return findById(periodicOperationList, id);
	}

	private Operation findById(Collection<? extends Operation> ops, String id) {
		for (Operation op : ops) {
			if (op.getId().equals(id)) {
				return op;
			}
		}
		return null;
	}

	protected void stop(StopHandler<Void> handler) {
		StopHandler<Operation> opStopHandler = new SchedulerStopHandler(handler);

		getOperationList.forEach(op -> op.stop(opStopHandler));
		setOperationList.forEach(op -> op.stop(opStopHandler));
		periodicOperationList.forEach(op -> op.stop(opStopHandler));
		asyncOperationList.forEach(op -> op.stop(opStopHandler));
	}

	/**
	 *
	 * @author Guido Rota (2014)
	 *
	 */
	private class SchedulerStopHandler implements StopHandler<Operation> {

		private final StopHandler<Void> parentStopHandler;
		private final Collection<Operation> operations;

		private SchedulerStopHandler(StopHandler<Void> parentStopHandler) {
			this.parentStopHandler = parentStopHandler;
			operations = new HashSet<>();
			operations.addAll(getOperationList);
			operations.addAll(setOperationList);
			operations.addAll(periodicOperationList);
			operations.addAll(asyncOperationList);
		}

		@Override
		public void hasStopped(Operation operation) {
			synchronized (operations) {
				if (!schedulable) {
					return;
				}

				operations.remove(operation);
				if (operations.isEmpty()) {
					schedulable = false;
					parentStopHandler.hasStopped(null);
				}
			}
		}

	}

}
