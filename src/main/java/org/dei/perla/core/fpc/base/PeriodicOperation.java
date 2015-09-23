package org.dei.perla.core.fpc.base;

import org.dei.perla.core.fpc.TaskHandler;
import org.dei.perla.core.fpc.Attribute;
import org.dei.perla.core.utils.Conditions;

import java.util.List;
import java.util.Map;

public abstract class PeriodicOperation extends BaseOperation<PeriodicTask> {

	private static final String SAMPLING_PERIOD = "period";

	// Global sampling period expressed in milliseconds
	protected long currentPeriod;

	public PeriodicOperation(String id, List<Attribute> atts) {
		super(id, atts);
		this.currentPeriod = 0;
	}

	public final synchronized long getSamplingPeriod() {
		return currentPeriod;
	}

	@Override
	protected PeriodicTask doSchedule(Map<String, Object> parameterMap,
			TaskHandler handler, SamplePipeline pipeline)
			throws IllegalArgumentException {
		long periodMs = getPeriod(parameterMap);

		PeriodicTask task = new PeriodicTask(this, handler, periodMs, pipeline);
		add(task);
		if (currentPeriod > periodMs || currentPeriod == 0) {
			setSamplingPeriod(periodMs);
		} else {
			task.setInputPeriod(currentPeriod);
		}

		return task;
	}

	protected final long getPeriod(Map<String, Object> parameterMap)
			throws IllegalArgumentException {
		long period;

		Conditions.checkIllegalArgument(
				parameterMap.containsKey(SAMPLING_PERIOD),
				"Missing sampling period in parameterMap");

		Object o = parameterMap.get(SAMPLING_PERIOD);
		if (o instanceof Long) {
			period = (long) o;
		} else if (o instanceof Integer) {
			period = (int) o;
		} else if (o instanceof Short) {
			period = (short) o;
		} else if (o instanceof Byte) {
			period = (byte) o;
		} else {
			throw new IllegalArgumentException(
					"Period parameter must be an integral value ("
							+ o.getClass().getSimpleName()
							+ "' received instead)");
		}

		Conditions.checkIllegalArgument(period >= 0,
				"Sampling period must be greater than zero");

		return period;
	}

	@Override
	protected void postRemove(List<PeriodicTask> tasks) {
		long min = minTaskPeriod(tasks);

		// All remaining task require a slower sampling rate, slow down
		if (min > currentPeriod) {
			setSamplingPeriod(min);
		}
	}

	/**
	 * Returns the slowest sampling period in all the running tasks
	 */
	private long minTaskPeriod(List<PeriodicTask> tasks) {
		long min = tasks.get(0).getPeriod();
		for (int i = 1; i < tasks.size(); i++) {
			long taskPeriod = tasks.get(i).getPeriod();
			if (min > taskPeriod) {
				min = taskPeriod;
			}
		}
		return min;
	}

	@Override
	protected void doStop() {
		setSamplingPeriod(0);
	}

	protected abstract void setSamplingPeriod(long period);

}
