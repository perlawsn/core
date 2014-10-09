package org.dei.perla.fpc.engine;

/**
 * Signals that the <code>Script</code> was cancelled.
 * 
 * 
 * @author Guido Rota (2014)
 *
 */
public class ScriptCancelledException extends ScriptException {

	private static final long serialVersionUID = -6882824150666276114L;

	public ScriptCancelledException() {
		super();
	}
	
	public ScriptCancelledException(String message) {
		super(message);
	}
	
	public ScriptCancelledException(Throwable cause) {
		super(cause);
	}
	
	public ScriptCancelledException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
