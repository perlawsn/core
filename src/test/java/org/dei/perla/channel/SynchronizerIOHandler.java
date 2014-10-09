package org.dei.perla.channel;

import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.dei.perla.channel.IOHandler;
import org.dei.perla.channel.IORequest;
import org.dei.perla.channel.Payload;

/**
 * {@code IOHandler} implementation for accessing synchronously to the
 * result of an asynchronous computation
 * 
 * @author Guido Rota (2014)
 *
 */
public class SynchronizerIOHandler implements IOHandler {

	private final Lock lock = new ReentrantLock();
	private final Condition doneCond = lock.newCondition();

	private boolean done = false;
	private Optional<Payload> result = null;
	private Throwable exception = null;

	public Optional<Payload> getResult() throws ExecutionException, InterruptedException {
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
	public void complete(IORequest request, Optional<Payload> result) {
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
	public void error(IORequest request, Throwable cause) {
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
