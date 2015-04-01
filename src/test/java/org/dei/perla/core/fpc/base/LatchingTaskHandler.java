package org.dei.perla.core.fpc.base;

import org.dei.perla.core.fpc.Task;
import org.dei.perla.core.fpc.TaskHandler;
import org.dei.perla.core.record.Record;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class LatchingTaskHandler implements TaskHandler {

	private final Lock lk = new ReentrantLock();
	private final Condition cond = lk.newCondition();

	private final int originalCount;

	private int waitCount;
	private int count = 0;

	private long previousTime = 0;
	private double avgPeriod = 0;

	private Throwable error;

	private final List<Record> samples = new ArrayList<>();

	public LatchingTaskHandler(int waitCount) {
		this.waitCount = waitCount;
		this.originalCount = waitCount;
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

	public List<Record> getSamples() throws InterruptedException {
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

	public Record getLastSample() throws InterruptedException {
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

	@Override
	public void complete(Task task) {
	}

	@Override
	public void newRecord(Task task, Record record) {
		lk.lock();
		try {
			if (waitCount <= 0) {
				return;
			}
			samples.add(record);
			waitCount--;
			count++;
			if (previousTime == 0) {
				previousTime = System.currentTimeMillis();
			} else {
				avgPeriod = (avgPeriod + (System.currentTimeMillis() - previousTime))
						/ count;
			}
			System.out.println(originalCount + " " + System.currentTimeMillis());
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
