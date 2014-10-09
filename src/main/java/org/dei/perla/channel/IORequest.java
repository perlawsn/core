package org.dei.perla.channel;


/**
 * <p>
 * A generic interface for requesting the execution of a <code>Channel</code>
 * operation. A default <code>IORequest</code> implementation is not supplied
 * since different <code>Channels</code> might require different parameters to
 * execute a user request.
 * </p>
 * 
 * <p>
 * A single <code>IORequest</code> may contain several <code>Payload</code>
 * objects, set through the <code>setParameter</code> method.
 * <code>Payload</code>s are used in a request for the following reasons:
 * <ul>
 * <li>Sending data to the remote device/service</li>
 * <li>Configuring the request with data that the <code>FPC</code> may
 * dynamically alter at runtime</li>
 * </ul>
 * </p>
 * 
 * @author Guido Rota (2014)
 * 
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
	 * 
	 * @param name
	 *            Name of the parameter to be set
	 * @param payload
	 *            Parameter payload
	 */
	public void setParameter(String name, Payload payload);

}
