package org.dei.perla.core.fpc.base;

import org.dei.perla.core.fpc.Task;
import org.dei.perla.core.fpc.TaskHandler;
import org.dei.perla.core.fpc.Sample;

/**
 * @author Guido Rota 23/05/15.
 */
public class ErrorTaskHandler implements TaskHandler {

    private Throwable error;
    private boolean complete = false;

    public synchronized Throwable awaitError() throws InterruptedException {
        while (error == null && !complete) {
            wait();
        }
        if (complete) {
            throw new RuntimeException("Complete without error");
        }
        return error;
    }

    @Override
    public synchronized void complete(Task task) {
        complete = true;
        notifyAll();
    }

    @Override
    public void data(Task task, Sample sample) {
        throw new RuntimeException("Exception injected for test purposes");
    }

    @Override
    public synchronized void error(Task task, Throwable cause) {
        error = cause;
        notifyAll();
    }

}
