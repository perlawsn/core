package org.dei.perla.channel.http;

import org.apache.log4j.Logger;
import org.dei.perla.channel.Channel;
import org.dei.perla.channel.ChannelFactory;
import org.dei.perla.fpc.descriptor.ChannelDescriptor;
import org.dei.perla.fpc.descriptor.InvalidDeviceDescriptorException;

public class HttpChannelFactory implements ChannelFactory {

	private static final String ERR_CHANNEL_CREATION = "Cannot create "
			+ HttpChannel.class.getCanonicalName() + ": %s";

	private final Logger logger = Logger.getLogger(HttpChannelFactory.class);

	@Override
	public Class<? extends ChannelDescriptor> acceptedChannelDescriptorClass() {
		return HttpChannelDescriptor.class;
	}

	public Channel createChannel(ChannelDescriptor descriptor)
			throws InvalidDeviceDescriptorException {
		if (!(descriptor instanceof HttpChannelDescriptor)) {
			String message = String.format(ERR_CHANNEL_CREATION, "expected "
					+ HttpChannelDescriptor.class.getCanonicalName()
					+ " but received "
					+ descriptor.getClass().getCanonicalName() + ".");
			logger.error(message);
			throw new InvalidDeviceDescriptorException(message);
		}
		return new HttpChannel(descriptor.getId());
	}

}
