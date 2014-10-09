package org.dei.perla.channel;

/**
 * <p>
 * This interface represents the state of execution of an <code>IORequest</code>
 * submitted to a <code>Channel</code>.
 * </p>
 * 
 * <p>
 * <code>IOTask</code> classes can be used to:
 * <ul>
 * <li>Retrieve the <code>IORequest</code> associated with the IOTask</li>
 * <li>Cancel an <code>IORequest</code> or query the completion status</li>
 * </ul>
 * </p>
 * 
 * @author Guido Rota (2014)
 * 
 */
public interface IOTask {

	/**
	 * Cancels the current <code>IOTask</code>. No changes are performed if the
	 * <code>IOTask</code> is complete or if it has already been cancelled.
	 */
	public void cancel();

	/**
	 * Returns the <code>IORequest</code> associated with this
	 * <code>IOTask</code>.
	 * 
	 * @return <code>IORequest</code> associated with this operation.
	 */
	public IORequest getRequest();

	/**
	 * Returns true after the <code>IOTask</code> is cancelled.
	 * 
	 * @return True if the <code>IOTask</code> was cancelled, false otherwise
	 */
	public boolean isCancelled();

	/**
	 * Returns true if the <code>IOTask</code> is complete. An
	 * <code>IOTask</code> is considered complete when it successfully finishes,
	 * when it finishes due to an error or when is cancelled.
	 * 
	 * @return True if the <code>IOTask</code> is complete, false otherwise
	 */
	public boolean isDone();

}
