package org.dei.perla.fpc.descriptor;

/**
 * Thrown to indicate that the <code>DeviceDescriptor</code> contains an error.
 * 
 * @author Guido Rota (2014)
 * 
 */
public class InvalidDeviceDescriptorException extends Exception {

	private static final long serialVersionUID = -3281091164247373760L;

	public InvalidDeviceDescriptorException() {
	}

	public InvalidDeviceDescriptorException(String message) {
		super(message);
	}

	public InvalidDeviceDescriptorException(Throwable cause) {
		super(cause);
	}

	public InvalidDeviceDescriptorException(String message, Throwable cause) {
		super(message, cause);
	}

}
