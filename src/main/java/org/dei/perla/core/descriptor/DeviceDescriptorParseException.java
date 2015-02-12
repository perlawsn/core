package org.dei.perla.core.descriptor;

/**
 * Thrown to indicate that the device descriptor data contains an error and
 * cannot be parsed correctly into the <code>DeviceDescriptor</code> object.
 *
 * This exception should only be used to indicate a problem in the parsing
 * procedure. Errors in the semantics of the parsed descriptor have to be
 * reported using the <code>InvalidDeviceDescriptorException</code> class.
 *
 * @author Guido Rota (2014)
 *
 */
public class DeviceDescriptorParseException extends Exception {

	private static final long serialVersionUID = 1963539081564488346L;

	public DeviceDescriptorParseException() {
		super();
	}

	public DeviceDescriptorParseException(String message) {
		super(message);
	}

	public DeviceDescriptorParseException(Throwable cause) {
		super(cause);
	}

	public DeviceDescriptorParseException(String message, Throwable cause) {
		super(message, cause);
	}

}
