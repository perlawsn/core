package org.dei.perla.core.fpc.base;

import org.dei.perla.core.fpc.Task;
import org.dei.perla.core.fpc.TaskHandler;
import org.dei.perla.core.fpc.Attribute;

import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * A {@link Task} that periodically creates a new {@link Sample} using only
 * static {@link Fpc} attributes.
 *
 * @author Guido Rota 02/07/15.
 */
public class StaticPeriodicTask implements Task {

    private final ScheduledThreadPoolExecutor pool;

    private final Request request;
    private final long period;
    private final TaskHandler handler;

    private ScheduledFuture task;

    public StaticPeriodicTask(Request request, long period,
            TaskHandler handler) {
        this.request = request;
        this.period = period;
        this.handler = handler;

        pool = new ScheduledThreadPoolExecutor(1);
    }

    public synchronized void start() {
        task = pool.scheduleAtFixedRate(this::sample, 0, period,
                TimeUnit.MILLISECONDS);
    }

    private synchronized void sample() {
        handler.data(this, request.generateSample());
    }

    @Override
    public List<Attribute> getAttributes() {
        return request.getGenerated();
    }

    @Override
    public synchronized boolean isRunning() {
        return task != null;
    }

    @Override
    public synchronized void stop() {
        task.cancel(false);
        handler.complete(this);
        task = null;
    }

}
