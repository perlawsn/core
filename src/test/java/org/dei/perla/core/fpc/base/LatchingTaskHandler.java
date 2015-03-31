package org.dei.perla.core.fpc.base;

import org.dei.perla.core.fpc.Task;
import org.dei.perla.core.fpc.TaskHandler;
import org.dei.perla.core.record.Record;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class LatchingTaskHandler implements TaskHandler {

	private volatile int count = 0;

	private volatile long previousTime = 0;
	private volatile double avgPeriod = 0;

	private final CountDownLatch latch;
	private volatile Throwable error;

	private final List<Record> samples = new ArrayList<>();

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

	public List<Record> getSamples() throws InterruptedException {
		latch.await();
		if (error != null) {
			throw new RuntimeException(error);
		}
		return samples;
	}

	public Record getLastSample() throws InterruptedException {
		latch.await();
		if (error != null) {
			throw new RuntimeException(error);
		}
		return samples.get(samples.size() - 1);
	}

	@Override
	public void complete(Task task) {
	}

	@Override
	public void newRecord(Task task, Record record) {
		samples.add(record);

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
