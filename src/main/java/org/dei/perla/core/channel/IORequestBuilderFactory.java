package org.dei.perla.core.channel;

import org.dei.perla.core.fpc.descriptor.IORequestDescriptor;
import org.dei.perla.core.fpc.descriptor.InvalidDeviceDescriptorException;

/**
 * A component for creating <code>IORequestBuilder</code> objects.
 *
 * @author Guido Rota (2014)
 *
 */
public interface IORequestBuilderFactory {

	/**
	 * Returns the <code>IORequestDescriptor</code> class that this
	 * <code>IORequestBuilderFactory</code> instance is able to parse.
	 *
	 * @return <code>IORequestDescriptor</code> class
	 */
	public Class<? extends IORequestDescriptor> acceptedIORequestClass();

	/**
	 * Creates a new <code>IORequestBuilder</code> using the information
	 * contained in the <code>IORequestDescriptor</code> object passed as
	 * parameter.
	 *
	 * @param descriptor
	 *            <code>IORequest</code> descriptor
	 * @return New <code>IORequestBuilder</code> instance
	 * @throws InvalidDeviceDescriptorException
	 *             when a wrong
	 */
	public IORequestBuilder create(IORequestDescriptor descriptor)
			throws InvalidDeviceDescriptorException;

}
