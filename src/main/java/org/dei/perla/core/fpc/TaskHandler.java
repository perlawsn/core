package org.dei.perla.core.fpc;

import org.dei.perla.core.fpc.engine.Record;

/**
 * <p>
 * A general handler interface for collecting the result of an asynchronous Fpc
 * {@link Task}
 * </p>
 *
 * <p>
 * The implementations of the {@code complete} and {@code error} methods should
 * terminate quickly so as to avoid blocking the invoking thread from continuing
 * normally.
 * </p>
 *
 * @author Guido Rota (2014)
 *
 */
public interface TaskHandler {

	/**
	 * <p>
	 * Invoked when the {@link Task} completes its execution successfully. This
	 * method is not invoked when the {@link Task} stops due to an error.
	 * </p>
	 *
	 * <p>
	 * No more records or errors will be produced after this method is invoked.
	 * </p>
	 *
	 * @param task
	 *            {@link Task} that completed its execution
	 */
	public void complete(Task task);

	/**
	 * Invoked when a new {@link Record} is ready
	 *
	 * @param task
	 *            {@link Task} that produced the new record
	 * @param result
	 *            Record
	 */
	public void newRecord(Task task, Record record);

	/**
	 * Invoked when error occurs with the Fpc {@link Task}. Invocation of this
	 * handler method does not imply that the underlying {@link Task} has
	 * stopped running. Thus, the user should always query the
	 * {@code Task.isRunning()} method to check if the error caused the
	 * {@link Task} to terminate or not.
	 *
	 * @param task
	 *            {@link task} that caused the error
	 * @param cause
	 *            Error cause
	 */
	public void error(Task task, Throwable cause);

}