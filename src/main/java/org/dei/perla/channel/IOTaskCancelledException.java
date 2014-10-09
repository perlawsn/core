package org.dei.perla.channel;

/**
 * Signals that the <code>IOTask</code> was cancelled
 * 
 * @author Guido Rota (2014)
 *
 */
public class IOTaskCancelledException extends Exception {

	private static final long serialVersionUID = -8598399058391914588L;

	public IOTaskCancelledException() {
		super();
	}
	
	public IOTaskCancelledException(String message) {
		super(message);
	}
	
	public IOTaskCancelledException(Throwable cause) {
		super(cause);
	}
	
	public IOTaskCancelledException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
