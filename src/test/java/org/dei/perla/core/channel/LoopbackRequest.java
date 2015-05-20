package org.dei.perla.core.channel;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class LoopbackRequest implements IORequest {

	private String message;

    private boolean paused = false;
    private boolean processed = false;
	private final Lock lk = new ReentrantLock();
    private final Condition cond = lk.newCondition();

	protected LoopbackRequest(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	@Override
	public String getId() {
		return "test";
	}

	@Override
	public void setParameter(String name, Payload payload) { }

    protected void setPaused() {
        lk.lock();
        try {
            if (paused || processed) {
                throw new IllegalStateException();
            }

            paused = true;
            cond.signalAll();
        } finally {
            lk.unlock();
        }
    }

    protected void setProcessed() {
        lk.lock();
        try {
            if (processed) {
                throw new IllegalStateException();
            }

            processed = true;
            cond.signalAll();
        } finally {
            lk.unlock();
        }
    }

    protected void waitPaused() throws InterruptedException {
        lk.lock();
        try {
            while (!paused && !processed) {
                cond.await();
            }

            if (processed && !paused) {
                throw new RuntimeException("Request finished processing " +
                        "without being put on pause");
            }
        } finally {
            lk.unlock();
        }
    }

    protected void waitProcessed() throws InterruptedException {
        lk.lock();
        try {
            while (!processed) {
                cond.await();
            }
        } finally {
            lk.unlock();
        }
    }

}
