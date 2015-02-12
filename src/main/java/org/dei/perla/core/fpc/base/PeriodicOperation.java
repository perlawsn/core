package org.dei.perla.core.fpc.base;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.dei.perla.core.fpc.Attribute;
import org.dei.perla.core.fpc.TaskHandler;
import org.dei.perla.core.utils.Conditions;

public abstract class PeriodicOperation extends AbstractOperation<PeriodicTask> {

	private static final String SAMPLING_PERIOD = "period";

	// Global sampling period expressed in milliseconds
	protected volatile long currentPeriod;

	public PeriodicOperation(String id, Collection<Attribute> attributes) {
		super(id, attributes);
		this.currentPeriod = 0;
	}

	public final long getSamplingPeriod() {
		return currentPeriod;
	}

	@Override
	protected PeriodicTask doSchedule(Map<String, Object> parameterMap,
			TaskHandler handler, RecordPipeline pipeline)
			throws IllegalArgumentException {
		long periodMs = getPeriodParameter(parameterMap);

		PeriodicTask task = new PeriodicTask(this, handler, periodMs, pipeline);
		add(task);
		if (periodMs < currentPeriod || currentPeriod == 0) {
			setSamplingPeriod(periodMs);
		} else {
			task.setInputPeriod(currentPeriod);
		}

		return task;
	}

	@Override
	protected void postRemove(List<PeriodicTask> taskList) {
		long minTaskPeriod = taskList.get(0).getPeriod();
		for (int i = 1; i < taskList.size(); i++) {
			long taskPeriod = taskList.get(i).getPeriod();
			if (minTaskPeriod > taskPeriod) {
				minTaskPeriod = taskPeriod;
			}
		}

		// All remaining task require a slower sampling rate, slow down
		if (minTaskPeriod > currentPeriod) {
			setSamplingPeriod(minTaskPeriod);
		}
	}

	protected abstract void setSamplingPeriod(long period);

	@Override
	protected void doStop() {
		setSamplingPeriod(0);
	}

	protected final long getPeriodParameter(Map<String, Object> parameterMap)
			throws IllegalArgumentException {
		long period;

		Conditions.checkIllegalArgument(
				parameterMap.containsKey(SAMPLING_PERIOD),
				"Missing sampling period in parameterMap");

		Object periodObj = parameterMap.get(SAMPLING_PERIOD);
		if (periodObj instanceof Long) {
			period = (long) periodObj;
		} else if (periodObj instanceof Integer) {
			period = (int) periodObj;
		} else if (periodObj instanceof Short) {
			period = (short) periodObj;
		} else if (periodObj instanceof Byte) {
			period = (byte) periodObj;
		} else {
			throw new IllegalArgumentException(
					"Period parameter must be an integral value ("
							+ periodObj.getClass().getSimpleName()
							+ "' received instead)");
		}

		Conditions.checkIllegalArgument(period >= 0,
				"Sampling period must be greater than zero");

		return period;
	}

}
