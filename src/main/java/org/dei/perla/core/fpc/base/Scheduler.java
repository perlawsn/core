package org.dei.perla.core.fpc.base;

import org.dei.perla.core.descriptor.DataType;
import org.dei.perla.core.record.Attribute;
import org.dei.perla.core.utils.StopHandler;

import java.util.*;

public class Scheduler {

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

	private final List<? extends Operation> get;
	private final List<? extends Operation> set;
	private final List<? extends Operation> periodic;
	private final List<? extends Operation> async;

	public Scheduler(List<? extends Operation> get,
            List<? extends Operation> set,
            List<? extends Operation> periodic,
            List<? extends Operation> async) {

		this.get = get;
		this.set = set;
		this.periodic = periodic;
		this.async = async;

		Collections.sort(this.get, attComp);
		Collections.sort(this.set, attComp);
		Collections.sort(this.periodic, attComp);
		Collections.sort(this.async, attComp);
	}

	protected Operation set(Collection<Attribute> atts)
			throws IllegalStateException {
		return bestFit(set, atts);
	}

	protected Operation get(List<Attribute> atts) throws IllegalStateException {
		return bestFit(get, atts);
	}

	protected Operation periodic(List<Attribute> atts)
			throws IllegalStateException {
		return bestFit(periodic, atts);
	}

	protected Operation async(List<Attribute> atts)
			throws IllegalStateException {
		return bestFit(async, atts);
	}

	private Operation bestFit(List<? extends Operation> ops,
			Collection<Attribute> atts) throws IllegalStateException {
		if (!schedulable) {
			throw new IllegalStateException("Scheduler has been stopped.");
		}

		for (Operation op : ops) {
			if (!hasAttributes(op, atts)) {
				continue;
			}
			return op;
		}
		return null;
	}

	private boolean hasAttributes(Operation o, Collection<Attribute> atts) {
		Collection<Attribute> ops = o.getAttributes();

		for (Attribute a : atts) {
			// Ignore timestamp attribute, since it will be added later if the
			// operation does not provide it natively (see method
			// createTimestampedPipeline)
			if (a.getId().compareToIgnoreCase(
					Attribute.TIMESTAMP.getId()) == 0
					&& a.getType() == DataType.TIMESTAMP) {
				continue;
			}
			if (!ops.contains(a)) {
				return false;
			}
		}

		return true;
	}

	protected Operation getGetOperation(String id) {
		return findById(get, id);
	}

	protected Operation getSetOperation(String id) {
		return findById(set, id);
	}

	protected Operation getPeriodicOperation(String id) {
		return findById(periodic, id);
	}

	private Operation findById(Collection<? extends Operation> ops, String id) {
		for (Operation op : ops) {
			if (op.getId().equals(id)) {
				return op;
			}
		}
		return null;
	}

	protected void stop(StopHandler<Void> h) {
		StopHandler<Operation> stopHandler = new SchedulerStopHandler(h);

		get.forEach(op -> op.stop(stopHandler));
		set.forEach(op -> op.stop(stopHandler));
		periodic.forEach(op -> op.stop(stopHandler));
		async.forEach(op -> op.stop(stopHandler));
	}

	/**
	 *
	 * @author Guido Rota (2014)
	 *
	 */
	private class SchedulerStopHandler implements StopHandler<Operation> {

		private final StopHandler<Void> parentStopHandler;
		private final Collection<Operation> ops;

		private SchedulerStopHandler(StopHandler<Void> parentStopHandler) {
			this.parentStopHandler = parentStopHandler;
			ops = new HashSet<>();
			ops.addAll(get);
			ops.addAll(set);
			ops.addAll(periodic);
			ops.addAll(async);
		}

		@Override
		public void hasStopped(Operation o) {
			synchronized (ops) {
				if (!schedulable) {
					return;
				}

				ops.remove(o);
				if (ops.isEmpty()) {
					schedulable = false;
					parentStopHandler.hasStopped(null);
				}
			}
		}

	}

}
