package org.dei.perla.core.channel;

import org.dei.perla.core.fpc.descriptor.ChannelDescriptor;
import org.dei.perla.core.fpc.descriptor.InvalidDeviceDescriptorException;

/**
 * A factory interface for creating new <code>Channel</code> instances.Each
 * different <code>Channel</code> type is created by a specific
 * <code>ChannelFactory</code> (e.g., HttpChannel is instantiated by the
 * HttpChannelFactory)
 *
 *
 * @author Guido Rota (2014)
 *
 */
public interface ChannelFactory {

	/**
	 * Returns the <code>ChannelDescriptor</code> class that this
	 * <code>ChannelFactory</code> instance is able to parse.
	 *
	 * @return ChannelDescriptor class
	 */
	public Class<? extends ChannelDescriptor> acceptedChannelDescriptorClass();

	/**
	 * <p>
	 * Creates a new <code>Channel</code> class.
	 * </p>
	 *
	 * <p>
	 * Note that the <code>MessageDescriptor</code> content is partially
	 * validated by the <code>FpcFactory</code> before being sent to a
	 * <code>ChannelFactory</code>. Consult the <code>FpcFactory</code> javadoc
	 * for a complete list of cheks performed on the
	 * <code>MessageDescriptor</code>.
	 * </p>
	 *
	 * @param descriptor
	 *            Java description of the <code>Channel</code>
	 * @return Channel instance
	 * @throws InvalidDeviceDescriptorException
	 */
	public Channel createChannel(ChannelDescriptor descriptor)
			throws InvalidDeviceDescriptorException;

}
