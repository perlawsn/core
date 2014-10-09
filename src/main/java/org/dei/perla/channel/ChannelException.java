package org.dei.perla.channel;

/**
 * Signals that an error occured while processing a {@code Channel IORequest}.
 * 
 * @author Guido Rota (2014)
 *
 */
public class ChannelException extends RuntimeException {

	private static final long serialVersionUID = -3601248885576500817L;

	public ChannelException() { super(); }
	
	public ChannelException(String message) {
		super(message);
	}
	
	public ChannelException(Throwable cause) {
		super(cause);
	}
	
	public ChannelException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
