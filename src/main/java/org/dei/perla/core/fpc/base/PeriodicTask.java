package org.dei.perla.core.fpc.base;

import org.dei.perla.core.fpc.TaskHandler;
import org.dei.perla.core.sample.SamplePipeline;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * A {@link Task} implementation for managing a periodic stream of data.
 *
 * <p>
 * This class can downsample the data coming from the sensing device in order
 * to obtain the output sampling period requested by the user.
 *
 * @author Guido Rota (2014)
 */
public class PeriodicTask extends BaseTask {

	// Downsampling data
	private long ratio = 0;
	private int error = 0;
	private long count = 0;

	private final long outputPeriod;

	protected PeriodicTask(PeriodicOperation operation, TaskHandler handler,
			long outputPeriod, SamplePipeline pipeline) {
		super(operation, handler, pipeline);
		this.outputPeriod = outputPeriod;
	}

	/**
	 * Returns the downsampling error, the ratio between the desired output
	 * data rate and the actual output data rate resulting from the
	 * downsampling operation (in percent).
	 *
	 * @return downsampling error in percent
	 */
	public final synchronized int errorPercent() {
		return error;
	}

	/**
	 * Returns the output period requested by the user.
	 *
	 * <p>
	 * It is important to note that the actual output period of the data relayed
	 * to the user may vary differ from the requested output period due to
	 * downsampling errors (see the {@code errorPercent()} method).
	 *
	 * @return output period in milliseconds
	 */
	protected final long getPeriod() {
		return outputPeriod;
	}

	/**
	 * Sets the input period of the data which is fed into the {@link
	 * PeriodicTask}. This information is employed to configure the
	 * downsampling ratio.
	 *
	 * @param inputPeriod input period of the raw data coming from the
	 *                       sensing device
	 */
	protected final synchronized void setInputPeriod(long inputPeriod) {
		BigDecimal ipBig = BigDecimal.valueOf(inputPeriod);
		BigDecimal opBig = BigDecimal.valueOf(outputPeriod);
		ratio = opBig.divide(ipBig, RoundingMode.HALF_EVEN).longValue();
		count = 0;

		if (outputPeriod % inputPeriod == 0) {
			error = 0;
		} else {
			error = (int) ((ratio * ((float) inputPeriod / outputPeriod) - 1) * 100);
		}
	}

	/**
	 * Process a new data sample through the {@link Task}.
	 *
	 * <p>
	 * This function relays the data sample to the associated {@link
	 * TaskHandler} and manages the downsampling operation.
	 *
	 * @param sample data sample
	 */
	protected final synchronized void newSample(Object[] sample) {
		if (!isRunning()) {
			return;
		}

		if (count == 0) {
			processSample(sample);
		}
		count = (count + 1) % ratio;
	}

}
