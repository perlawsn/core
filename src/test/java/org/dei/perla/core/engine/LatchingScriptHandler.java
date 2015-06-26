package org.dei.perla.core.engine;

import java.util.List;

/**
 * @author Guido Rota 26/06/15.
 */
public class LatchingScriptHandler implements ScriptHandler {

    private final int count;
    private int completed = 0;
    private Throwable error = null;

    protected LatchingScriptHandler(int count) {
        this.count = count;
    }

    protected synchronized void await() throws InterruptedException {
        while (completed < count && error == null) {
            wait();
        }

        if (error != null) {
            throw new RuntimeException("Script error occurred", error);
        }
    }

    @Override
    public synchronized void complete(Script script, List<Object[]> samples) {
        completed++;
        if (completed >= count) {
            notifyAll();
        }
    }

    @Override
    public synchronized void error(Script script, Throwable cause) {
        error = cause;
        notifyAll();
    }

}
