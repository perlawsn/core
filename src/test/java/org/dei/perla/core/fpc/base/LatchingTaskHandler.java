package org.dei.perla.core.fpc.base;

import java.util.concurrent.CountDownLatch;

import org.dei.perla.core.fpc.Task;
import org.dei.perla.core.fpc.TaskHandler;
import org.dei.perla.core.record.Record;

public class LatchingTaskHandler implements TaskHandler {

	private volatile int count = 0;

	private volatile long previousTime = 0;
	private volatile double avgPeriod = 0;

	private final CountDownLatch latch;
	private volatile Throwable error;

	private volatile Record lastRecord = null;

	public LatchingTaskHandler(int waitCount) {
		latch = new CountDownLatch(waitCount);
	}

	public int getCount() throws InterruptedException {
		latch.await();
		if (error != null) {
			throw new RuntimeException(error);
		}
		return count;
	}

	public double getAveragePeriod() throws InterruptedException {
		latch.await();
		if (error != null) {
			throw new RuntimeException(error);
		}
		return avgPeriod;
	}

	public Record getLastRecord() throws InterruptedException {
		latch.await();
		if (error != null) {
			throw new RuntimeException(error);
		}
		return lastRecord;
	}

	@Override
	public void complete(Task task) {
	}

	@Override
	public void newRecord(Task task, Record record) {
		lastRecord = record;

		if (previousTime == 0) {
			previousTime = System.currentTimeMillis();
			return;
		}

		count++;
		latch.countDown();
		avgPeriod = (avgPeriod + (System.currentTimeMillis() - previousTime))
				/ count;
	}

	@Override
	public void error(Task task, Throwable cause) {
		error = cause;
		while (latch.getCount() > 0) {
			latch.countDown();
		}
	}

}
