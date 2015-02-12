package org.dei.perla.core.fpc.engine;

/**
 * An exception signalling an abnormal <code>Script</code> execution condition.
 *
 * @author Guido Rota (2014)
 *
 */
public class ScriptException extends Exception {

	private static final long serialVersionUID = 1L;

	public ScriptException() {
		super();
	}

	public ScriptException(String message) {
		super(message);
	}

	public ScriptException(Throwable cause) {
		super(cause);
	}

	public ScriptException(String message, Throwable cause) {
		super(message, cause);
	}

}
