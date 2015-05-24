package org.dei.perla.core.engine;

import java.util.List;

/**
 * A general handler interface for collecting the result of an asynchronous
 * {@link Script}.
 *
 * <p>
 * The implementations of the {@code complete} and {@code error} methods should
 * terminate quickly so as to avoid blocking the invoking thread from continuing
 * normally.
 *
 * @author Guido Rota (2014)
 *
 */
public interface ScriptHandler {

	/**
	 * Invoked when an {@link Script} completes successfully
	 *
	 * @param script The Script that generated the data samples
	 * @param samples Result of the Script
	 */
	public void complete(Script script, List<Object[]> samples);

	/**
	 * Invoked when an {@link Script} completes with an error
	 *
	 * @param script The Script that triggered the error
	 * @param cause Error cause
	 */
	public void error(Script script, Throwable cause);

}
