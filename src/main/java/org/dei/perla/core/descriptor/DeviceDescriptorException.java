package org.dei.perla.core.descriptor;

/**
 * @author Guido Rota 18/05/15.
 */
public class DeviceDescriptorException extends Exception {

    private static final long serialVersionUID = 1963539081564488346L;

    public DeviceDescriptorException() {
        super();
    }

    public DeviceDescriptorException(String message) {
        super(message);
    }

    public DeviceDescriptorException(Throwable cause) {
        super(cause);
    }

    public DeviceDescriptorException(String message, Throwable cause) {
        super(message, cause);
    }

}
