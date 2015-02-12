package org.dei.perla.core.channel;

import java.util.Optional;

/**
 * <p>
 * A general handler interface for collecting the result of an asynchronous
 * <code>IOTask</code>.
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
public interface IOHandler {

	/**
	 * Invoked when an <code>IOTask</code> completes successfully
	 *
	 * @param request
	 *            <code>IORequest</code> corresponding to the completed task
	 * @param result
	 *            Result of the operation
	 */
	public void complete(IORequest request, Optional<Payload> result);

	/**
	 * Invoked when an <code>IOTask</code> completes with an error
	 *
	 * @param request
	 *            <code>IORequest</code> corresponding to the completed task
	 * @param cause
	 *            Error cause
	 */
	public void error(IORequest request, Throwable cause);

}
