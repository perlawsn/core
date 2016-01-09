package org.dei.perla.core.fpc.base;

import org.dei.perla.core.engine.Executor;
import org.dei.perla.core.engine.Script;
import org.dei.perla.core.engine.ScriptHandler;
import org.dei.perla.core.engine.ScriptParameter;
import org.dei.perla.core.fpc.TaskHandler;
import org.dei.perla.core.message.FpcMessage;
import org.dei.perla.core.message.Mapper;
import org.dei.perla.core.fpc.Attribute;
import org.dei.perla.core.utils.AsyncUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.function.Consumer;

public final class AsyncOperation
        extends BaseOperation<AsyncOperation.AsyncTask> {

    private static final int STOPPED = 0;
    private static final int SUSPENDED = 1;
    private static final int STARTED = 2;

    // Thread pool for simulated periodic Operation
    private static final ScheduledThreadPoolExecutor executor;
    static {
        executor = new ScheduledThreadPoolExecutor(10);
        executor.setRemoveOnCancelPolicy(true);
    }

    private final Script startScript;

    private int state;

    private final AsyncMessageHandler asyncHandler;
    private final OnHandler onHandler = new OnHandler();

    private volatile Object[] sample;

    protected AsyncOperation(String id, List<Attribute> atts,
            Script startScript, AsyncMessageHandler handler,
            ChannelManager channelMgr) {
        super(id, atts);
        this.startScript = startScript;
        this.asyncHandler = handler;

        sample = new Object[atts.size()];

        state = STOPPED;
        channelMgr.addCallback(asyncHandler.mapper, this::handleMessage);
    }

    protected void start() {
        runStartScript();
    }

    private void runStartScript() {
        if (startScript != null) {
            Executor.execute(startScript, new StartHandler());
        }
    }

    protected synchronized Object[] getSampleCopy() {
        return Arrays.copyOf(sample, sample.length);
    }

    @Override
    public AsyncTask doSchedule(Map<String, Object> parameterMap,
            TaskHandler handler, SamplePipeline pipeline)
            throws IllegalArgumentException {
        AsyncTask task = new AsyncTask(this, handler, pipeline);
        add(task);
        return task;
    }

    public void handleMessage(FpcMessage message) {
        ScriptParameter paramArray[] = new ScriptParameter[1];
        paramArray[0] = new ScriptParameter(asyncHandler.variable, message);

        Executor.execute(asyncHandler.script, paramArray, onHandler);
    }

    @Override
    public void doStop() {
        state = STOPPED;
    }

    @Override
    public void doStop(Consumer<Operation> handler) {
        doStop();
        // Invoke in new thread to preserve asynchronous locking semantics
        AsyncUtils.runInNewThread(() -> {
            handler.accept(this);
        });
    }

    /**
     * Operation startup script handler
     *
     * @author Guido Rota (2014)
     */
    private class StartHandler implements ScriptHandler {

        @Override
        public void complete(Script script, List<Object[]> samples) {
            synchronized (AsyncOperation.this) {
                state = STARTED;
            }
        }

        @Override
        public void error(Script script, Throwable cause) {
            synchronized (AsyncOperation.this) {
                if (state == SUSPENDED) {
                    return;
                }
                state = SUSPENDED;
                String message = "Error starting asynchronous operation";
                log.error(message, cause);
            }
        }

    }

    /**
     * Data script handler
     *
     * @author Guido Rota (2014)
     */
    private class OnHandler implements ScriptHandler {

        @Override
        public void complete(Script script, List<Object[]> samples) {
            synchronized (AsyncOperation.this) {
                samples.forEach(s -> forEachTask(t -> t.processSample(s)));
                int last = samples.size() - 1;
                sample = samples.get(last);
            }
        }

        @Override
        public void error(Script script, Throwable cause) {
            synchronized (AsyncOperation.this) {
                log.error("Execution error in 'on' script", cause);
                forEachTask(t -> t.notifyError(cause, false));
            }
        }

    }

    /**
     * Implementation of a {@link org.dei.perla.core.fpc.Task} for managing
     * asynchronous data transmissions.
     *
     * @author Guido Rota
     */
    protected class AsyncTask extends BaseTask {

        public AsyncTask(BaseOperation<?> operation, TaskHandler handler,
                         SamplePipeline pipeline) {
            super(operation, handler, pipeline);
        }

    }

    /**
     * Simple container class for managing the association between {@link
     * Script}s and {@link Mapper}s.
     *
     * @author Guido Rota
     */
    public static class AsyncMessageHandler {

        private final Mapper mapper;
        private final Script script;
        private final String variable;

        public AsyncMessageHandler(Mapper mapper, Script script, String variable) {
            this.mapper = mapper;
            this.script = script;
            this.variable = variable;
        }

    }

}
