package org.dei.perla.core.fpc.base;

import org.apache.log4j.Logger;
import org.dei.perla.core.fpc.Attribute;
import org.dei.perla.core.fpc.FpcException;
import org.dei.perla.core.fpc.TaskHandler;
import org.dei.perla.core.utils.Check;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * <p>
 * An abstract implementation of the {@link BaseOperation} interface. This
 * class provides a common implementation of several housekeeping methods needed
 * by all {@link Operation} classes.
 * </p>
 *
 * <p>
 * This implementation keeps a list of all tasks scheduled by the operation.
 * Imlementing classes are responsible for managing the addition or removal of
 * {@link Task}s using the {@code add()} and {@code remove()} methods.
 * </p>
 *
 * @author Guido Rota (2014)
 *
 */
public abstract class BaseOperation<T extends BaseTask>
        implements Operation {

    protected final Logger log;

    private final String id;
    private final List<Attribute> atts;

    private final SamplePipeline defPipeline;

    private boolean schedulable;

    private volatile List<T> tasks = new ArrayList<>();

    /**
     * {@code BaseOperation} constructor.
     *
     * @param id
     *            Operation identifier
     * @param atts
     *            Collection of {@link Attribute}s generated by the
     *            {@link Operation}
     */
    public BaseOperation(String id, List<Attribute> atts) {
        this.log = Logger.getLogger(this.getClass().getSimpleName() + "_" + id);
        this.id = id;
        this.atts = Collections.unmodifiableList(atts);
        schedulable = true;
        defPipeline = new SamplePipeline(atts);
    }

    @Override
    public final String getId() {
        return id;
    }

    @Override
    public final List<Attribute> getAttributes() {
        return atts;
    }

    @Override
    public final synchronized boolean isSchedulable() {
        return schedulable;
    }

    @Override
    public final BaseTask schedule(Map<String, Object> params, TaskHandler h)
            throws IllegalArgumentException, IllegalStateException {
        return schedule(params, h, defPipeline);
    }

    @Override
    public final synchronized BaseTask schedule(
            Map<String, Object> params, TaskHandler h, SamplePipeline p)
            throws IllegalArgumentException, IllegalStateException {
        Check.notNull(h, "handler");
        if (!schedulable) {
            throw new IllegalStateException(
                    "Operation '" + id + "' is not schedulable");
        }

        // Wrapping the invocation inside a lock ensures that the scheduling
        // operations are run in mutual exclusion with all other methods
        // that may modify the internal task list or the operating status of
        // this operation
        return doSchedule(params, h, p);
    }

    /**
     * Performs the actual scheduling of a new {@link Task}.
     *
     * This method is executed in mutual exclusion with all other operation that
     * may change the task list content or the internal state of this
     * {@link BaseOperation}.
     *
     * @param params
     *            Parameters to be passed
     * @param h
     *            {@link TaskHandler} object used to asynchronously collect the
     *            {@code Operation} output
     * @return {@link Task} object for controlling the {@code Operation}
     *         execution
     * @throws IllegalArgumentException
     *             When the parameters required to run this operation are
     *             notfound in the parameterMap
     * @throws IllegalStateException
     *             If the {@code schedule} method is invoked when the
     *             {@code Operation} is not running
     */
    protected abstract T doSchedule(Map<String, Object> params, TaskHandler h,
            SamplePipeline p) throws IllegalArgumentException;

    @Override
    public final synchronized void stop(Consumer<Operation> h) {
        if (!schedulable) {
            return;
        }

        tasks.forEach(T::operationStopped);
        tasks.clear();
        doStop(h);
        schedulable = false;
    }

    /**
     * Method invoked to stop the {@code BaseOperation}. It can be
     * overridden if a custom stop behaviour is required.
     */
    protected abstract void doStop();

    /**
     * Method invoked to stop the {@code BaseOperation}. It can be
     * overridden if a custom stop behaviour is required.
     *
     * This version of the {@code doStop()} method takes a {@link StopHandler}
     * argument to asynchronously notify the caller about the correct shutdown
     * of the {@link Operation}. All implementations of the
     * {@code BaseOperation} class must invoke the
     * {@code StopHandler.hasStopped()} method when the operation has been
     * properly stopped. Failure to due so may lead other {@code Fpc} components
     * to fail at shutdown.
     *
     * @param handler
     *            StopHandler invoked when the operation is corectly terminated
     */
    protected abstract void doStop(Consumer<Operation> handler);

    /**
     * Adds an {@link BaseTask} to the list of tasks scheduled by this
     * {@code BaseOperation}.
     *
     * This method is executed in mutual exclusion with all other operation that
     * may change the task list content or the internal state of this
     * {@link BaseOperation}.
     *
     * @param task
     *            {@link BaseTask} to be added
     */
    protected final synchronized void add(T task) {
        tasks.add(task);
    }

    /**
     * <p>
     * Removes a {@link BaseTask} from the list of task scheduled by this
     * {@code BaseOperation}.
     *
     * <p>
     * This method is executed in mutual exclusion with all other operation that
     * may change the task list content or the internal state of this
     * {@link BaseOperation}.
     *
     * @param task
     *            {@link BaseTask} to be removed
     */
    protected final synchronized void remove(BaseTask task) {
        if (!tasks.contains(task)) {
            return;
        }
        tasks.remove(task);

        if (tasks.isEmpty()) {
            doStop();
            return;
        }

        postRemove(Collections.unmodifiableList(tasks));
    }

    /**
     * Method invoked after a {@link Task} is removed from the internal task
     * list. It is intended to be overridden by concrete
     * {@code BaseOperation} implementations.
     *
     * This method will not be invoked when the task list is empty. In this
     * case, the {@code doStop()} method will be called instead.
     *
     * This method is executed in mutual exclusion with all other operation that
     * may change the task list content or the internal state of this
     * {@link BaseOperation}.
     *
     * @param tasks
     *            List of remaining {@link Task}s
     */
    protected void postRemove(List<T> tasks) { }

    /**
     * Stops all active {@link Task}s and signal the cause of the error that
     * prompted this action.
     *
     * This method is executed in mutual exclusion with all other operation that
     * may change the task list content or the internal state of this
     * {@link BaseOperation}.
     *
     * @param msg
     *            Error message
     * @param cause
     *            Cause of the unrecoverable error
     */
    protected final synchronized void unrecoverableError(String msg,
            Throwable cause) {
        log.error("Unexpected error, stopping operation", cause);
        FpcException e = new FpcException(msg, cause);
        tasks.forEach(t -> t.operationError(e));
        tasks.clear();
        doStop();
    }

    ///////////////////
    // Utility methods
    ///////////////////

    /**
     * Returns the number of {@link Task}s currently scheduled on this
     * {@code BaseOperation}
     *
     * @return Number of scheduled {@link Task}s
     */
    protected final synchronized int taskCount() {
        return tasks.size();
    }

    /**
     * <p>
     * Performs the operation passed as parameter for all registered
     * {@link PeriodicTask}s
     *
     * <p>
     * NOTE: This function is not thread-safe. For proper synchronization, a
     * lock to the Operation object must be acquired.
     *
     * @param operation
     *            Operation to execute
     */
    public final void forEachTask(Consumer<T> op) {
        tasks.forEach(op);
    }

}
