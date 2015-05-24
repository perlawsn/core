package org.dei.perla.core.engine;

import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Guido Rota 24/05/15.
 */
public class ErrorScriptHandler implements ScriptHandler {

    private final Lock lk = new ReentrantLock();
    private final Condition cond = lk.newCondition();

    private Throwable error = null;

    public Throwable awaitError() throws InterruptedException {
        lk.lock();
        try {
            while (error == null) {
                cond.await();
            }
            return error;
        } finally {
            lk.unlock();
        }
    }

    @Override
    public void complete(Script script, List<Object[]> samples) {
        throw new RuntimeException("Exception injected for test purposes");
    }

    @Override
    public void error(Throwable cause) {
        lk.lock();
        try {
            error = cause;
            cond.signalAll();
        } finally {
            lk.unlock();
        }
    }

}
