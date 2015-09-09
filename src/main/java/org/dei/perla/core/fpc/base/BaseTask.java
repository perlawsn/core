package org.dei.perla.core.fpc.base;

import org.apache.log4j.Logger;
import org.dei.perla.core.fpc.FpcException;
import org.dei.perla.core.fpc.Task;
import org.dei.perla.core.fpc.TaskHandler;
import org.dei.perla.core.fpc.Attribute;
import org.dei.perla.core.fpc.Sample;
import org.dei.perla.core.fpc.SamplePipeline;
import org.dei.perla.core.utils.AsyncUtils;

import java.util.List;

/**
 * An abstract implementation of the {@link Task} interface. It is the base
 * class implemented by all {@link Task}s scheduled from an {@link
 * BaseOperation}.
 *
 * @author Guido Rota (2014)
 *
 */
public abstract class BaseTask implements Task {

    protected final Logger log;

    private boolean hasStarted = false;
    private boolean running = false;

    private final BaseOperation<? extends BaseTask> op;
    private final SamplePipeline pipeline;
    private final List<Attribute> atts;
    private final TaskHandler handler;

    /**
     * Instantiates a new {@code BaseTask}.
     *
     * @param op
     *            {@link BaseOperation} from which this {@code BaseTask}
     *            was scheduled
     * @param handler
     *            {@link TaskHandler} employed to notify the presence of new
     *            {@link Sample}s to other interested users.
     * @param pipeline
     *            {@link SamplePipeline} used to process new {@link Sample}
     *            prior to notifying them to the {@link TaskHandler}.
     */
    public BaseTask(BaseOperation<? extends BaseTask> op,
                    TaskHandler handler, SamplePipeline pipeline) {
        this.op = op;
        this.handler = handler;
        this.pipeline = pipeline;
        this.atts = pipeline.atts;

        log = Logger.getLogger(op.getClass().getSimpleName() + " task");
    }

    @Override
    public final List<Attribute> getAttributes() {
        return atts;
    }

    @Override
    public final synchronized boolean isRunning() {
        return running;
    }

    /**
     * Returns the {@link Operation} used to schedule this {@link Task}
     *
     * @return {@link Operation} that scheduled this {@link Task}
     */
    protected final Operation getOperation() {
        return op;
    }

    /**
     * Starts the {@link Task}.
     */
    protected final synchronized void start() {
        if (hasStarted) {
            throw new IllegalStateException("Cannot start," +
                    "BaseTask has already been started once");
        }
        running = true;
        hasStarted = true;
        doStart();
    }

    /**
     * A method invoked whenever the {@code BaseTask} is started. It can be
     * overridden by concrete {@code BaseTask} implementation to add custom
     * startup behaviour.
     */
    protected void doStart() {}

    @Override
    public final void stop() {
        // Acquiring locks in the same order as they are acquired in the
        // corresponding Operation (Operation first, then Task) to avoid
        // deadlock
        synchronized (op) {
            synchronized (this) {
                if (!running) {
                    return;
                }
                running = false;
                doStop();
                op.remove(this);
                // Invoke in new thread to preserve asynchronous locking semantics
                AsyncUtils.runInNewThread(() -> handler.complete(this));
            }
        }
    }

    /**
     * A method invoked whenever the {@code BaseTask} is stopped. It can be
     * overridden by concrete {@code BaseTask} implementation to add custom
     * shutdown behaviour.
     */
    protected void doStop() {}


    //////////////////////////////////////
    // Methods invoked by parent Operation
    //////////////////////////////////////

    /**
     * Immediately cancels the {@link BaseTask} execution following an error
     * occurred to the connected {@link Operation}.
     *
     * <p>
     * This method is intended to be called by an {@link Operation} object to
     * indicate that an exception occurred while processing a script. Invoking
     * this method will not remove the task from the {@link BaseOperation}'s
     * task list.
     *
     * <p>
     * Invoking this method does not produce any effect if the
     * {@code BaseTask} is stopped
     *
     * @param cause
     *            Cause of the error
     */
    protected final synchronized void operationError(FpcException cause) {
        if (!running) {
            return;
        }
        running = false;
        doStop();
        handler.error(this, cause);
    }

    /**
     * Immediately stops the {@link BaseTask} execution.
     *
     * <p>
     * This method is intended to be called by an {@link BaseOperation}
     * object to indicate that no more samples will be produced. Invoking this
     * method will not remove the task from the {@link BaseOperation}'s task
     * list.
     *
     * <p>
     * Invoking this method does not produce any effect if the
     * {@code BaseTask} is stopped
     */
    protected final synchronized void operationStopped() {
        if (!running) {
            return;
        }
        running = false;
        doStop();
        handler.complete(this);
    }


    /////////////////////////////////////////////////////////////////
    // Methods invoked by BaseTask children and controlling Operation
    /////////////////////////////////////////////////////////////////

    /**
     * Runs the a new {@link Sample} in the {@link SamplePipeline} and handles
     * it over to the registered {@link TaskHandler}. This method is intended to
     * be invoked by a {@link BaseOperation} whenever a new sample is
     * produced by the remote device.
     *
     * <p>
     * Invoking this method does not produce any effect if the
     * {@code BaseTask} is stopped
     *
     * @param sample
     *            sample to be processed
     */
    protected final synchronized void processSample(Object[] sample) {
        if (!running) {
            return;
        }
        Sample output = pipeline.run(sample);
        handler.data(this, output);
    }

    /**
     * Invokes the registered {@link TaskHandler} to inform any interested
     * object that the {@link Task} is complete, and that no new {@link Sample}
     * are going to be produced.
     *
     * <p>
     * This method is intended to be used by an BaseTask subclass
     *
     * <p>
     * Invoking this method does not produce any effect if the
     * {@code BaseTask} is stopped
     */
    protected final void notifyComplete() {
        // Acquiring locks in the same order as they are acquired in the
        // corresponding Operation (Operation first, then Task) to avoid
        // deadlock
        synchronized (op) {
            synchronized (this) {
                if (!running) {
                    return;
                }
                running = false;
                op.remove(this);
                handler.complete(this);
            }
        }
    }

    /**
     * Invokes the registered {@link TaskHandler} to inform any interested
     * object that an error has occurred. The additional {@code stop} parameter
     * may be used to indicate whether the error is unrecoverable (no new
     * {@link Sample}s will be produced) or not.
     *
     * <p>
     * This method is intended to be used by an BaseTask subclass
     *
     * <p>
     * Invoking this method does not produce any effect if the
     * {@code BaseTask} is stopped
     *
     * @param cause
     *            Cause of the error
     * @param stop
     *            Stops the {@link BaseTask} if set to true
     */
    protected final void notifyError(Throwable cause, boolean stop) {
        // Acquiring locks in the same order as they are acquired in the
        // corresponding Operation (Operation first, then Task) to avoid
        // deadlock
        synchronized (op) {
            synchronized (this) {
                if (!running) {
                    return;
                }
                if (stop && running) {
                    running = false;
                    op.remove(this);
                }
                handler.error(this, cause);
            }
        }
    }

}
