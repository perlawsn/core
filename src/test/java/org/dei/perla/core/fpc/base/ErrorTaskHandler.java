package org.dei.perla.core.fpc.base;

import org.dei.perla.core.fpc.Task;
import org.dei.perla.core.fpc.TaskHandler;
import org.dei.perla.core.sample.Sample;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Guido Rota 23/05/15.
 */
public class ErrorTaskHandler implements TaskHandler {

    private final Lock lk = new ReentrantLock();
    private final Condition cond = lk.newCondition();

    private Throwable error;
    private boolean complete = false;

    public Throwable awaitError() throws InterruptedException {
        lk.lock();
        try {
            while (error == null && !complete) {
                cond.await();
            }
            if (complete) {
                throw new RuntimeException("Complete without error");
            }
            return error;
        } finally {
            lk.unlock();
        }
    }

    @Override
    public void complete(Task task) {
        lk.lock();
        try {
            complete = true;
            cond.signalAll();
        } finally {
            lk.unlock();
        }
    }

    @Override
    public void data(Task task, Sample sample) {
        throw new RuntimeException("Exception injected for test purposes");
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
