package org.dei.perla.core.channel;

import org.dei.perla.core.Plugin;

/**
 * @author Guido Rota 18/05/15.
 */
public interface ChannelPlugin extends Plugin {

    public void registerFactoryHandler(IOHandler handler);

    public ChannelFactory getChannelFactory();

    public IORequestBuilderFactory getIORequestBuilderFactory();

}
