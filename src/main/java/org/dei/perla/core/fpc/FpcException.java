package org.dei.perla.core.fpc;

/**
 * Indicates that a faulty condition occurred in an {@link Fpc}.
 *
 * @author Guido Rota (2014)
 *
 */
public class FpcException extends Exception {

    private static final long serialVersionUID = -6325730173990268880L;

    public FpcException() {
        super();
    }

    public FpcException(String message) {
        super(message);
    }

    public FpcException(Throwable cause) {
        super(cause);
    }

    public FpcException(String message, Throwable cause) {
        super(message, cause);
    }

}
