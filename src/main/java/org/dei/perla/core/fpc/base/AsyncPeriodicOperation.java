package org.dei.perla.core.fpc.base;

import org.dei.perla.core.utils.AsyncUtils;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * @author Guido Rota 25/06/15.
 */
public final class AsyncPeriodicOperation extends PeriodicOperation {

    private final ScheduledThreadPoolExecutor executor;

    private final AsyncOperation op;
    private volatile ScheduledFuture<?> timerFuture = null;

    public AsyncPeriodicOperation(AsyncOperation op) {
        super("Simulated one-off (async) " + op.getId(), op.getAttributes());
        this.op = op;

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
    protected void setSamplingPeriod(long period) {
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

    private synchronized void sample() {
        Object[] sample = op.getSampleCopy();
        forEachTask(t -> t.newSample(sample));
    }

    @Override
    protected void doStop(Consumer<Operation> handler) {
        // Invoke in new thread to preserve asynchronous locking semantics
        AsyncUtils.runInNewThread(() -> {
            handler.accept(this);
        });
    }


}
