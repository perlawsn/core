package org.dei.perla.core.fpc;

/**
 * An exception indicating that the {@link FpcFactory} encountered an error
 * while creating a new {@link Fpc}.
 *
 * @author Guido Rota 02/07/15.
 *
 */
public class FpcCreationException extends Exception {

    private static final long serialVersionUID = -3796486929785532276L;

    public FpcCreationException() {
        super();
    }

    public FpcCreationException(String message) {
        super(message);
    }

    public FpcCreationException(Throwable cause) {
        super(cause);
    }

    public FpcCreationException(String message, Throwable cause) {
        super(message, cause);
    }

}
