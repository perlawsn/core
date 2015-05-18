package org.dei.perla.core.channel.injector;

import org.dei.perla.core.channel.IORequestBuilder;
import org.dei.perla.core.channel.IORequestBuilderFactory;
import org.dei.perla.core.descriptor.IORequestDescriptor;
import org.dei.perla.core.descriptor.InvalidDeviceDescriptorException;

/**
 * @author Guido Rota 18/05/15.
 */
public class InjectorIORequestBuilderFactory implements IORequestBuilderFactory {

    @Override
    public Class<? extends IORequestDescriptor> acceptedIORequestClass() {
        return InjectorIORequestDescriptor.class;
    }

    @Override
    public IORequestBuilder create(IORequestDescriptor descriptor) throws InvalidDeviceDescriptorException {
        throw new UnsupportedOperationException();
    }

}
