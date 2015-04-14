package org.dei.perla.core.channel;

/**
 * <p>
 * An abstract communication channel between <code>FPC</code>s and physical
 * devices. Each concrete <code>Channel</code> implementation manages a single
 * communication protocol. Concrete <code>Channel</code> instances are created
 * by means of a <code>ChannelFactory</code>. Each <code>Channel</code> instance
 * is meant to be used by a single <code>FPC</code> only.
 * </p>
 *
 * <p>
 * <code>Channel</code> invocations are asynchronous by nature; Users send
 * requests by means of the <code>submit</code> method, and retrieve results by
 * means of <code>IOHandler</code>s. Request execution may be monitored or
 * cancelled using the <code>IOTask</code> object returned upon submitting a new
 * <code>IORequest</code>.
 * </p>
 *
 * <p>
 * The <code>Channel</code> is also designed to handle asynchronous data
 * transfer operations initiated by the remote device, a scenario that may
 * present itself whenever the node independently streams a series of data
 * samples to the PerLa system. <code>Payload</code> data corresponding to such
 * communications is made available to a single <code>IOHandler</code>
 * registered with the <code>setAsyncIOHandler</code> method.
 * </p>
 *
 * <p>
 * <code>Channel</code> classes should implement a complete communication
 * protocol that allows the <code>FPC</code> to effectively and efficiently
 * communicate with the remote device or service that they abstract. Therefore,
 * PerLa programmers should favour the implementation of multiple high-level,
 * moderately specialized <code>Channel</code>s over a limited set of generic,
 * multipurpose <code>Channel</code>s, since the latter design would put too
 * much overhead on both the <code>FPC</code> and device descriptor writer.
 * Ideally, PerLa <code>Channel</code>s should implement protocols corresponding
 * to the Application layer of the OSI model.
 * </p>
 *
 * <p>
 * Moving protocol information information in the <code>Channel</code> is
 * beneficial in that it promotes use of readily available third-party
 * communication libraries. As an example, a <code>Channel</code> implementation
 * of the FTP protocol could use one of the immediately available FPT Java
 * libraries. This would not be feasible if application and transport logic were
 * to be split between the <code>FPC</code> and the <code>Channel</code>.
 * </p>
 *
 * @author Guido Rota (2014)
 *
 */
public interface Channel {

	/**
	 * Returns the {@code Channel} identifier
	 *
	 * @return {@code Channel} identifier
	 */
	public String getId();

	/**
	 * <p>
	 * Submits a new <code>IORequest</code> to be performed by the
	 * <code>Channel</code>, and returns a <code>IOTask</code> for controlling
	 * the execution status of the requested operation.
	 * </p>
	 *
	 * <p>
	 * Calls to this method return immediately, and the submitted
	 * <code>IORequest</code> is performed asynchronously. The result of the
	 * <code>Channel</code> operation is made available using the
	 * <code>IOHandler</code> passed as parameter.
	 * </p>
	 *
	 * @param request
	 *            <code>IORequest</code> to be performed by the Channel
	 * @param handler
	 *            <code>IOHandler</code> for asynchronous <code>Channel</code>
	 *            management
	 * @return An <code>IOTask</code> object that can be used to control the
	 *         request execution and to retrieve the <code>Channel</code>
	 *         response
	 * @throws ChannelException
	 *             If the <code>Channel</code> is unable to perform the
	 *             <code>IORequest</code> being submitted (e.g., Channel closed)
	 */
	public IOTask submit(IORequest request, IOHandler handler)
			throws ChannelException;

	/**
	 * <p>
	 * Sets a <code>IOHandler</code> to be used by the <code>Channel</code> for
	 * dispatching the results of asynchronous communications, i.e. data
	 * transfers initiated by the remote device or service without an explicit
	 * request from the PerLa framework.
	 * </p>
	 *
	 * <p>
	 * Unlike the <code>IOHandler</code> used in the <code>submit()</code>
	 * method, no <code>IORequest</code> is passed when the handler is invoked
	 * in response to an asynchronous communication, since no request was made
	 * to trigger the communication.
	 * </p>
	 *
	 * <p>
	 * No more than a single <code>IOHandler</code> can be set on each
	 * <code>Channel</code>.
	 * </p>
	 *
	 * @param handler
	 *            <code>IOHandler</code> to be used
	 * @throws IllegalStateException
	 *             if this method is called when a <code>IOHandler</code> was
	 *             already set for this <code>Channel</code>
	 */
	public void setAsyncIOHandler(IOHandler handler)
			throws IllegalStateException;

	/**
	 * Returns true if the <code>Channel</code> is closed
	 *
	 * @return true if the <code>Channel</code> is closed, false otherwise
	 */
	public boolean isClosed();

	/**
	 * <p>
	 * Closes the <code>Channel</code>, stops listening for new
	 * <code>IORequest</code> and relinquishes any resource held.
	 * </p>
	 *
	 * <p>
	 * After closing the <code>Channel</code>, all <code>IORequests</code>
	 * waiting to be executed are cancelled and notified through the appropriate
	 * <code>IOHandler</code>.
	 * </p>
	 */
	public void close();

}
