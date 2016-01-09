package org.dei.perla.core.fpc;

/**
 * <p>
 * A general handler interface for collecting the result of an asynchronous Fpc
 * {@link Task}
 *
 * <p>
 * The implementations of the {@code complete} and {@code error} methods should
 * terminate quickly so as to avoid blocking the invoking thread from continuing
 * normally.
 *
 * @author Guido Rota (2014)
 */
public interface TaskHandler {

	/**
	 * <p>
	 * Invoked when the {@link Task} completes its execution successfully. This
	 * method is not invoked when the {@link Task} stops due to an error.
	 *
	 * <p>
	 * No more samples or errors will be produced after this method is invoked.
	 *
	 * @param task {@link Task} that completed its execution
	 */
	public void complete(Task task);

	/**
	 * Invoked when a new {@link Sample} is ready
	 *
	 * @param task {@link Task} that produced the new sample
	 * @param sample new sample
	 */
	public void data(Task task, Sample sample);

	/**
	 * Invoked when error occurs with the Fpc {@link Task}. Invocation of this
	 * handler method does not imply that the underlying {@link Task} has
	 * stopped running. Thus, the user should always query the
	 * {@code Task.isRunning()} method to check if the error caused the
	 * {@link Task} to terminate or not.
	 *
	 * @param task
	 *            {@link Task} that caused the error
	 * @param cause
	 *            Error cause
	 */
	public void error(Task task, Throwable cause);

}
