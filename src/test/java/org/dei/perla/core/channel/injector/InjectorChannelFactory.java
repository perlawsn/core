package org.dei.perla.core.channel.injector;

import org.dei.perla.core.channel.Channel;
import org.dei.perla.core.channel.ChannelFactory;
import org.dei.perla.core.descriptor.ChannelDescriptor;
import org.dei.perla.core.descriptor.InvalidDeviceDescriptorException;

/**
 * @author Guido Rota 18/05/15.
 */
public class InjectorChannelFactory implements ChannelFactory {

    @Override
    public Class<? extends ChannelDescriptor> acceptedChannelDescriptorClass() {
        return InjectorChannelDescriptor.class;
    }

    @Override
    public Channel createChannel(ChannelDescriptor descriptor) throws InvalidDeviceDescriptorException {
        throw new UnsupportedOperationException();
    }

}
