package org.dei.perla.core.channel.injector;

import org.dei.perla.core.channel.*;

/**
 * @author Guido Rota 18/05/15.
 */
public class InjectorChannel implements Channel {

    @Override
    public String getId() {
        throw new UnsupportedOperationException();
    }

    @Override
    public IOTask submit(IORequest request, IOHandler handler) throws ChannelException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setAsyncIOHandler(IOHandler handler) throws IllegalStateException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isClosed() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() {
        throw new UnsupportedOperationException();

    }

}
