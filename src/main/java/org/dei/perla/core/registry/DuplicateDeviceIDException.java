package org.dei.perla.core.registry;

/**
 * @author Guido Rota 19/05/15.
 */
public class DuplicateDeviceIDException extends Exception {

    private static final long serialVersionUID = -6081923960004104627L;

    public DuplicateDeviceIDException() { }

    public DuplicateDeviceIDException(String msg) {
        super(msg);
    }

    public DuplicateDeviceIDException(Throwable cause) {
        super(cause);
    }

    public DuplicateDeviceIDException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
