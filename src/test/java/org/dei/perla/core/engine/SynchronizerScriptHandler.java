package org.dei.perla.core.engine;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * {@code ScriptHandler} implementation for accessing synchronously to the
 * result of an asynchronous computation
 *
 * @author Guido Rota (2014)
 *
 */
public class SynchronizerScriptHandler implements ScriptHandler {

	private final Lock lock = new ReentrantLock();
	private final Condition doneCond = lock.newCondition();

	private boolean done = false;
	private List<Object[]> samples = null;
	private Throwable exception = null;

	public List<Object[]> getResult() throws ExecutionException,
			InterruptedException {
		lock.lock();
		try {

			while (!done) {
				doneCond.await();
			}
			if (exception != null) {
				throw new ExecutionException(exception);
			}
			return samples;

		} finally {
			lock.unlock();
		}
	}

	@Override
	public void complete(Script script, List<Object[]> samples) {
		lock.lock();
		try {
			this.samples = samples;
			done = true;
			doneCond.signal();
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void error(Script script, Throwable cause) {
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
