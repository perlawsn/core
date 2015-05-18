package org.dei.perla.core.channel.injector;

import org.dei.perla.core.channel.*;

import java.util.Optional;

/**
 * A Channel plugin employed to test the Fpc creation functionality of the
 * PerLaSystem class
 *
 * @author Guido Rota 18/05/15.
 */
public class InjectorChannelPlugin implements ChannelPlugin {

    private IOHandler fctHand;

    public void injectDescriptor(Payload payload) {
        fctHand.complete(null, Optional.ofNullable(payload));
    }

    @Override
    public void registerFactoryHandler(IOHandler handler) {
        fctHand = handler;
    }

    @Override
    public ChannelFactory getChannelFactory() {
        return new InjectorChannelFactory();
    }

    @Override
    public IORequestBuilderFactory getIORequestBuilderFactory() {
        return new InjectorIORequestBuilderFactory();
    }

}
