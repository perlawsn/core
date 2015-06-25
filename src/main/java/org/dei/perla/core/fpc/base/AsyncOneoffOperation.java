package org.dei.perla.core.fpc.base;

import org.dei.perla.core.fpc.TaskHandler;
import org.dei.perla.core.sample.SamplePipeline;
import org.dei.perla.core.utils.AsyncUtils;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Simulated oneoff operation simulated from asynchronous data
 *
 * @author Guido Rota 25/06/15.
 */
public class AsyncOneoffOperation extends BaseOperation<BaseTask> {

    private final AsyncOperation op;

    public AsyncOneoffOperation(AsyncOperation op) {
        super("Simulated one-off (async) " + op.getId(), op.getAttributes());
        this.op = op;
    }

    @Override
    protected AsyncOneoffTask doSchedule(Map<String, Object> params,
            TaskHandler h, SamplePipeline p) throws IllegalArgumentException {
        return new AsyncOneoffTask(this, h, p);
    }

    @Override
    protected void doStop() {}

    @Override
    protected void doStop(Consumer<Operation> handler) {
        // Synchronization and AsyncUtils.runInNewThread ensure that the
        // handler is effectively asynchronously called after the doStop
        // invocation is has been completed.
        AsyncUtils.runInNewThread(() -> {
            synchronized (AsyncOneoffOperation.this) {
                handler.accept(this);
            }
        });
    }

    /**
     * Task implementation for simulating oneoff operations from asynchronous
     * operations
     *
     * @author Guido Rota
     */
    private class AsyncOneoffTask extends BaseTask {

        public AsyncOneoffTask(AsyncOneoffOperation op,
                TaskHandler handler, SamplePipeline pipeline) {
            super(op, handler, pipeline);
        }

        @Override
        protected synchronized void doStart() {
            Object[] sample = op.getSampleCopy();
            this.processSample(sample);
            this.notifyComplete();
        }

    }

}
