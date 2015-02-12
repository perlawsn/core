package org.dei.perla.core.fpc.base;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.dei.perla.core.fpc.TaskHandler;
import org.dei.perla.core.engine.Record;

public class PeriodicTask extends AbstractTask {

	// Downsampling data
	private long ratio = 0;
	private int error = 0;
	private long count = 0;

	private final long outputPeriod;

	protected PeriodicTask(PeriodicOperation operation, TaskHandler handler,
			long outputPeriod, RecordPipeline pipeline) {
		super(operation, handler, pipeline);
		this.outputPeriod = outputPeriod;
	}

	public int errorPercent() {
		return error;
	}

	protected long getPeriod() {
		return outputPeriod;
	}

	protected void setInputPeriod(long inputPeriod) {
		BigDecimal ipBig = BigDecimal.valueOf(inputPeriod);
		BigDecimal opBig = BigDecimal.valueOf(outputPeriod);
		ratio = opBig.divide(ipBig, RoundingMode.HALF_EVEN).longValue();

		if (outputPeriod % inputPeriod == 0) {
			error = 0;
		} else {
			error = (int) ((ratio * ((float) inputPeriod / outputPeriod) - 1) * 100);
		}
	}

	@Override
	public void doStop() {
		ratio = 0;
	}

	protected void newRecord(Record record) {
		if (ratio == 0) {
			return;
		}

		if (count == 0) {
			processRecord(record);
		}
		count = (count + 1) % ratio;
	}

}
