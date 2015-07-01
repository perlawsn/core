package org.dei.perla.core.engine;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * {@code ScriptHandler} implementation for accessing synchronously to the
 * result of an asynchronous computation
 *
 * @author Guido Rota (2014)
 *
 */
public class SynchronizerScriptHandler implements ScriptHandler {

    private boolean done = false;
    private List<Object[]> samples = null;
    private Throwable exception = null;

    public synchronized List<Object[]> getResult() throws ExecutionException,
        InterruptedException {
        while (!done && exception == null) {
            wait();
        }
        if (exception != null) {
            throw new ExecutionException(exception);
        }
        return samples;
    }

    @Override
    public synchronized void complete(Script script, List<Object[]> samples) {
        this.samples = samples;
        done = true;
        notifyAll();
    }

    @Override
    public synchronized void error(Script script, Throwable cause) {
        exception = cause;
        done = true;
        notifyAll();
    }

}
