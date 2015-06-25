package org.dei.perla.core.fpc.base;

import org.dei.perla.core.engine.Executor;
import org.dei.perla.core.engine.Script;
import org.dei.perla.core.engine.ScriptHandler;
import org.dei.perla.core.fpc.FpcException;
import org.dei.perla.core.utils.AsyncUtils;

import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public final class SimulatedPeriodicOperation extends PeriodicOperation {

    private final ScheduledThreadPoolExecutor executor;

    private final Script script;

    private volatile ScheduledFuture<?> timerFuture = null;
    private final TimerScriptHandler handler = new TimerScriptHandler();

    public SimulatedPeriodicOperation(String id, Script script) {
        super(id, script.getEmit());
        this.script = script;
        timerFuture = null;

        // A single executor thread, combined with the synchronous script
        // execution model (see the sample method), ensures that all script
        // invocations are executed sequentially. If multiple threads were
        // used instead the Java scheduler could reorder or aggregate
        // the single script executions, causing a more or less severe
        // alteration of the effective sampling period.
        executor = new ScheduledThreadPoolExecutor(1);
        executor.setRemoveOnCancelPolicy(true);
    }

    @Override
    protected void setSamplingPeriod(final long period) {
        if (timerFuture != null) {
            timerFuture.cancel(false);
        }
        if (period == 0) {
            currentPeriod = 0;
            return;
        }

        currentPeriod = period;
        forEachTask(t -> t.setInputPeriod(period));
        timerFuture = executor.scheduleAtFixedRate(this::sample,
                period, period, TimeUnit.MILLISECONDS);
    }

    private void sample() {
        try {
            Executor.execute(script, Executor.EMPTY_PARAMETER_ARRAY, handler);
            // The synchronous execution of the sampling script ensures that
            // all script invocations are performed sequentially.
            handler.await();

        } catch (Exception e) {
            handler.error(script, new RuntimeException("Unexpected error " +
                    "while running simulated periodic operation", e));
        }
    }

    @Override
    protected void doStop(Consumer<Operation> handler) {
        executor.shutdownNow();
        // Synchronization and AsyncUtils.runInNewThread ensure that the
        // handler is effectively asynchronously called after the doStop
        // invocation is has been completed.
        AsyncUtils.runInNewThread(() -> {
            synchronized (SimulatedPeriodicOperation.this) {
                handler.accept(this);
            }
        });
    }

    /**
     * Timer handler, distributes the outcome of the script to all the
     * {@link Task} objects.
     */
    private class TimerScriptHandler implements ScriptHandler {

        public synchronized void await() throws InterruptedException {
            this.wait();
        }

        @Override
        public synchronized void complete(Script script,
                List<Object[]> samples) {
            synchronized (SimulatedPeriodicOperation.this) {
                for (Object[] s : samples) {
                    forEachTask(t -> t.newSample(s));
                }
                this.notify();
            }
        }

        @Override
        public synchronized void error(Script script, Throwable cause) {
            synchronized (SimulatedPeriodicOperation.this) {
                Exception e = new FpcException(cause);
                forEachTask(t -> t.notifyError(e, false));
                setSamplingPeriod(0); // Stop the operation
                this.notify();
            }
        }

    }

}
