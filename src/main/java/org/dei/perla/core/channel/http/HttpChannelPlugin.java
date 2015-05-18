package org.dei.perla.core.channel.http;

import org.dei.perla.core.channel.ChannelFactory;
import org.dei.perla.core.channel.ChannelPlugin;
import org.dei.perla.core.channel.IOHandler;

/**
 * @author Guido Rota 18/05/15.
 */
public class HttpChannelPlugin implements ChannelPlugin {

    public final HttpChannelFactory chFct;
    public final HttpIORequestBuilderFactory ioreqFct;

    public HttpChannelPlugin() {
        chFct = new HttpChannelFactory();
        ioreqFct = new HttpIORequestBuilderFactory();
    }

    @Override
    public void registerFactoryHandler(IOHandler handler) { }

    @Override
    public ChannelFactory getChannelFactory() {
        return chFct;
    }

    @Override
    public HttpIORequestBuilderFactory getIORequestBuilderFactory() {
        return ioreqFct;
    }

}
