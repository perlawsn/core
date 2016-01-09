package org.dei.perla.core.channel;


/**
 * A generic interface for requesting the execution of a {@link Channel}
 * operation. A default {@link IORequest} implementation is not supplied
 * since different {@link Channel}s might require different parameters to
 * execute a user request.
 *
 * A single {@link IORequest} may contain several {@link Payload}
 * objects, set through the {@link IORequest#setParameter} method.
 * {@link Payload}s are used in a request for the following reasons:
 * <ul>
 * <li>Sending data to the remote device/service</li>
 * <li>Configuring the request with data that the
 * {@link org.dei.perla.core.fpc.Fpc} may dynamically alter at runtime</li>
 * </ul>
 *
 * @author Guido Rota (2014)
 */
public interface IORequest {

    /**
     * Returns the identifier of this <code>IORequest</code>
     *
     * @return Request identifier
     */
    public String getId();

    /**
     * Set a parameter of an <code>IORequest</code>. This method is mainly used
     * by the <code>FPC</code> to populate the <code>IORequest</code> object
     * with data that dinamically change between requests.
     *
     * @param name    Name of the parameter to be set
     * @param payload Parameter payload
     */
    public void setParameter(String name, Payload payload);

}
