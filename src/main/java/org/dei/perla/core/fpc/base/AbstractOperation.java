package org.dei.perla.core.fpc.base;

import org.apache.log4j.Logger;
import org.dei.perla.core.fpc.FpcException;
import org.dei.perla.core.fpc.Task;
import org.dei.perla.core.fpc.TaskHandler;
import org.dei.perla.core.record.Attribute;
import org.dei.perla.core.record.RecordPipeline;
import org.dei.perla.core.utils.Conditions;
import org.dei.perla.core.utils.StopHandler;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * <p>
 * An abstract implementation of the {@link AbstractOperation} interface. This
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
public abstract class AbstractOperation<T extends AbstractTask> implements
        Operation {

	protected final Logger log;

	private final String id;
	private final List<Attribute> atts;

	// State
	private final Lock lk = new ReentrantLock();
	private volatile boolean schedulable;

	// Unsynchronized acces to the taskList when reading. Reads on the
	// taskList will greatly outnumber the writes, since this list is traversed
	// every time a record is produced by the remote device.
	private volatile List<T> tasks = new CopyOnWriteArrayList<>();

	/**
	 * {@code AbstractOperation} constructor.
	 *
	 * @param id
	 *            Operation identifier
	 * @param atts
	 *            Collection of {@link Attribute}s generated by the
	 *            {@link Operation}
	 */
	public AbstractOperation(String id, List<Attribute> atts) {
		this.id = id;
		this.atts = Collections.unmodifiableList(atts);
		schedulable = true;
		this.log = Logger.getLogger(this.getClass().getSimpleName() + "_" + id);
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
	public final boolean isSchedulable() {
		return schedulable;
	}

	@Override
	public final AbstractTask schedule(Map<String, Object> params, TaskHandler h,
            RecordPipeline p) throws IllegalArgumentException, IllegalStateException {
		Conditions.checkNotNull(h, "handler");
		if (!schedulable) {
			throw new IllegalStateException("Operation '" + id
					+ "' is not schedulable");
		}

		lk.lock();
		try {

			// Wrapping the invocation inside a lock ensures that the scheduling
			// operations are run in mutual exclusion with all other methods
			// that may modify the internal task list or the operating status of
			// this operation
			return doSchedule(params, h, p);

		} finally {
			lk.unlock();
		}
	}

	/**
	 * Performs the actual scheduling of a new {@link Task}.
	 *
	 * This method is executed in mutual exclusion with all other operation that
	 * may change the task list content or the internal state of this
	 * {@link AbstractOperation}.
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
            RecordPipeline p) throws IllegalArgumentException;

	@Override
	public final void stop(StopHandler<Operation> h) {
		if (!schedulable) {
			return;
		}

		lk.lock();
		try {

			tasks.forEach(T::operationStopped);
			tasks.clear();
			doStop(h);
			schedulable = false;

		} finally {
			lk.unlock();
		}
	}

	/**
	 * Method invoked to stop the {@code AbstractOperation}. It can be
	 * overridden if a custom stop behaviour is required.
	 */
	protected abstract void doStop();

	/**
	 * Method invoked to stop the {@code AbstractOperation}. It can be
	 * overridden if a custom stop behaviour is required.
	 *
	 * This version of the {@code doStop()} method takes a {@link StopHandler}
	 * argument to asynchronously notify the caller about the correct shutdown
	 * of the {@link Operation}. All implementations of the
	 * {@code AbstractOperation} class must invoke the
	 * {@code StopHandler.hasStopped()} method when the operation has been
	 * properly stopped. Failure to due so may lead other {@code Fpc} components
	 * to fail at shutdown.
	 *
	 * @param handler
	 *            StopHandler invoked when the operation is corectly terminated
	 */
	protected abstract void doStop(StopHandler<Operation> handler);

	/**
	 * Adds an {@link AbstractTask} to the list of tasks scheduled by this
	 * {@code AbstractOperation}.
	 *
	 * This method is executed in mutual exclusion with all other operation that
	 * may change the task list content or the internal state of this
	 * {@link AbstractOperation}.
	 *
	 * @param task
	 *            {@link AbstractTask} to be added
	 */
	protected final void add(T task) {
		lk.lock();
		try {
			tasks.add(task);
		} finally {
			lk.unlock();
		}
	}

	/**
	 * Removes an {@link AbstractTask} from the list of task scheduled by this
	 * {@code AbstractOperation}.
	 *
	 * This method is executed in mutual exclusion with all other operation that
	 * may change the task list content or the internal state of this
	 * {@link AbstractOperation}.
	 *
	 * @param task
	 *            {@link AbstractTask} to be removed
	 */
	protected final void remove(AbstractTask task) {
		lk.lock();
		try {

			if (!tasks.contains(task)) {
				return;
			}
			tasks.remove(task);

			if (tasks.isEmpty()) {
				doStop();
				return;
			}

			postRemove(Collections.unmodifiableList(tasks));

		} finally {
			lk.unlock();
		}
	}

	/**
	 * Method invoked after a {@link Task} is removed from the internal task
	 * list. It is intended to be overridden by concrete
	 * {@code AbstractOperation} implementations.
	 *
	 * This method will not be invoked when the task list is empty. In this
	 * case, the {@code doStop()} method will be called instead.
	 *
	 * This method is executed in mutual exclusion with all other operation that
	 * may change the task list content or the internal state of this
	 * {@link AbstractOperation}.
	 *
	 * @param tasks
	 *            List of remaining {@link Task}s
	 */
	protected void postRemove(List<T> tasks) {
	}

	/**
	 * Stops all active {@link Task}s and signal the cause of the error that
	 * prompted this action.
	 *
	 * This method is executed in mutual exclusion with all other operation that
	 * may change the task list content or the internal state of this
	 * {@link AbstractOperation}.
	 *
	 * @param msg
	 *            Error message
	 * @param cause
	 *            Cause of the unrecoverable error
	 */
	protected final void unrecoverableError(String msg, Throwable cause) {
		lk.lock();
		try {

			log.error("Unexpected error, stopping operation", cause);
			FpcException e = new FpcException(msg, cause);
			tasks.forEach(t -> t.operationError(e));
			tasks.clear();
			doStop();

		} finally {
			lk.unlock();
		}
	}

	// ////////////////
	// Utility methods
	// ////////////////

	/**
	 * Returns the number of {@link Task}s currently scheduled on this
	 * {@code AbstractOperation}
	 *
	 * @return Number of scheduled {@link Task}s
	 */
	protected final int taskCount() {
		return tasks.size();
	}

	/**
	 * Performs the function passed as parameter in mutual exclusion with all
	 * other methods that may change the internal task list or that may modify
	 * the {@code Operation} state.
	 *
	 * @param operation
	 *            Operation to perform
	 */
	protected final void runUnderLock(Runnable op) {
		lk.lock();
		try {
			op.run();
		} finally {
			lk.unlock();
		}
	}

	/**
	 * Performs the function passed as parameter in mutual exclusion with all
	 * other methods that may change the internal task list or that may modify
	 * the {@code Operation} state.
	 *
	 * @param operation
	 *            Operation to perform
	 */
	protected final <E> E runUnderLock(Supplier<E> op) {
		lk.lock();
		try {
			return op.get();
		} finally {
			lk.unlock();
		}
	}

	/**
	 * Performs the operation passed as parameter for all registered
	 * {@link PeriodicTask}s
	 *
	 * @param operation
	 *            Operation to execute
	 */
	public final void forEachTask(Consumer<T> op) {
        // No synchronization needed on read-only operation, since the task
        // list has a copy-on-write semantics
		tasks.forEach(op);
	}

}
