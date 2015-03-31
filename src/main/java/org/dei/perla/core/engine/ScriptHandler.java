package org.dei.perla.core.engine;

import java.util.List;

/**
 * A general handler interface for collecting the result of an asynchronous
 * <code>Script</code>.
 *
 * The implementations of the {@code complete} and {@code error} methods should
 * terminate quickly so as to avoid blocking the invoking thread from continuing
 * normally.
 *
 * @author Guido Rota (2014)
 *
 */
public interface ScriptHandler {

	/**
	 * Invoked when an <code>Script</code> completes successfully
	 *
	 * @param result
	 *            Result of the operation
	 */
	public void complete(Script script, List<Object[]> samples);

	/**
	 * Invoked when an <code>Script</code> completes with an error
	 *
	 * @param cause
	 *            Error cause
	 */
	public void error(Throwable cause);

}
