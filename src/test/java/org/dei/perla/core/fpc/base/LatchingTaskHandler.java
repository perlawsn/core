package org.dei.perla.core.fpc.base;

import org.dei.perla.core.fpc.Task;
import org.dei.perla.core.fpc.TaskHandler;
import org.dei.perla.core.sample.Sample;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class LatchingTaskHandler implements TaskHandler {

	private final Lock lk = new ReentrantLock();
	private final Condition cond = lk.newCondition();

	private int waitCount;
	private int count = 0;

	private Instant previousTime = null;
	private double avgPeriod = 0;

	private Throwable error;

	private final List<Sample> samples = new ArrayList<>();

	public LatchingTaskHandler(int waitCount) {
		this.waitCount = waitCount;
	}

	public int getCount() throws InterruptedException {
		lk.lock();
		try {
			if (waitCount > 0) {
				cond.await();
			}
			if (error != null) {
				throw new RuntimeException(error);
			}
			return count;
		} finally {
			lk.unlock();
		}
	}

	public double getAveragePeriod() throws InterruptedException {
		lk.lock();
		try {
			if (waitCount > 0) {
				cond.await();
			}
			if (error != null) {
				throw new RuntimeException(error);
			}
			return avgPeriod;
		} finally {
			lk.unlock();
		}
	}

	public List<Sample> getSamples() throws InterruptedException {
		lk.lock();
		try {
			if (waitCount > 0) {
				cond.await();
			}
			if (error != null) {
				throw new RuntimeException(error);
			}
			return samples;
		} finally {
			lk.unlock();
		}
	}

	public Sample getLastSample() throws InterruptedException {
		lk.lock();
		try {
			if (waitCount > 0) {
				cond.await();
			}
			if (error != null) {
				throw new RuntimeException(error);
			}
			return samples.get(samples.size() - 1);
		} finally {
			lk.unlock();
		}
	}

	public void awaitCompletion() throws InterruptedException {
		lk.lock();
		try {
			if (waitCount > 0) {
				cond.await();
			}
		} finally {
			lk.unlock();
		}
	}

	@Override
	public void complete(Task task) {
		lk.lock();
		try {
			waitCount = 0;
			cond.signalAll();
		} finally {
			lk.unlock();
		}
	}

	@Override
	public void data(Task task, Sample sample) {
		lk.lock();
		try {
			samples.add(sample);
			waitCount--;
			count++;

			Instant ts;
			if (sample.hasField("timestamp")) {
				ts = (Instant) sample.getValue("timestamp");
			} else {
				ts = Instant.now();
			}
			if (previousTime == null) {
				previousTime = ts;
			} else {
				long diff = ts.toEpochMilli() - previousTime.toEpochMilli();
				avgPeriod = (avgPeriod + diff) / count;
			}
			if (waitCount == 0) {
				cond.signalAll();
			}
		} finally {
			lk.unlock();
		}
	}

	@Override
	public void error(Task task, Throwable cause) {
		lk.lock();
		try {
			error = cause;
			cond.signalAll();
		} finally {
			lk.unlock();
		}
	}

}
