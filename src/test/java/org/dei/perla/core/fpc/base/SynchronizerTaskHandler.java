package org.dei.perla.core.fpc.base;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.dei.perla.core.fpc.Task;
import org.dei.perla.core.fpc.TaskHandler;
import org.dei.perla.core.fpc.engine.Record;

/**
 * {@code ScriptHandler} implementation for accessing synchronously to the
 * result of an asynchronous computation
 *
 * @author Guido Rota (2014)
 *
 */
public class SynchronizerTaskHandler implements TaskHandler {

	private final Lock lock = new ReentrantLock();
	private final Condition doneCond = lock.newCondition();

	private boolean done = false;
	private Record result = null;
	private Throwable exception = null;

	public Record getResult() throws ExecutionException, InterruptedException {
		lock.lock();
		try {

			while (!done) {
				doneCond.await();
			}
			if (exception != null) {
				throw new ExecutionException(exception);
			}
			return result;

		} finally {
			lock.unlock();
		}
	}

	@Override
	public void complete(Task task) {
		lock.lock();
		try {
			done = true;
			doneCond.signal();
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void newRecord(Task task, Record result) {
		lock.lock();
		try {
			this.result = result;
			done = true;
			doneCond.signal();
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void error(Task task, Throwable cause) {
		lock.lock();
		try {
			this.exception = cause;
			done = true;
			doneCond.signal();
		} finally {
			lock.unlock();
		}
	}

}
