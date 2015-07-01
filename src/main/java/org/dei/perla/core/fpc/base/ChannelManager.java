package org.dei.perla.core.fpc.base;

import org.apache.log4j.Logger;
import org.dei.perla.core.channel.Channel;
import org.dei.perla.core.channel.IOHandler;
import org.dei.perla.core.channel.IORequest;
import org.dei.perla.core.channel.Payload;
import org.dei.perla.core.message.FpcMessage;
import org.dei.perla.core.message.Mapper;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p> The {@code ChannelManager} is a component responsible for managing
 * multiple asynchronous callbacks registered on the available {@link Fpc}
 * {@link Channel}s.
 *
 * <p> Every callback registered in the {@code ChannelManager} is
 * associated with a specific message type. When a registered message type is
 * received, the associated callback function is invoked.
 *
 * <p> Message types are identified by means of the associated {@link Mapper}
 * class. This entails that there can only be a single callback function for
 * each message type.
 *
 * @author Guido Rota (2015)
 *
 */
public final class ChannelManager {

    private static final Logger logger = Logger.getLogger(ChannelManager.class);

    private final List<Channel> channels;
    private final Map<Mapper, AsyncChannelCallback> callbacks =
            new ConcurrentHashMap<>();

    public ChannelManager(List<Channel> channels) {
        this.channels = channels;

        // Set the asynchronous handler on every channel
        IOHandler h = new IOHandler() {
            @Override
            public void complete(IORequest request, Optional<Payload> result) {
                asyncDispatch(result);
            }

            @Override
            public void error(IORequest request, Throwable cause) {
                logger.warn("Error in asynchronous receive", cause);
            }
        };
        this.channels.forEach(c -> c.setAsyncIOHandler(h));
    }

    /**
     * Registers a new callback function with a particular message type,
     * identified by the {@link Mapper} parameter. The callback function will
     * be invoked every time the associated message type is received by any
     * of the {@link Channel}s managed by the {@link ChannelManager}
     *
     * @param mapper {@link Mapper} object identifying the message type
     *                             bound to the callback function
     * @param callback callback function to be invoked once the associated
     *                 message is received
     */
    public void addCallback(Mapper mapper, AsyncChannelCallback callback) {
        callbacks.put(mapper, callback);
    }

    /**
     * Removes the callback function associated with the {@link Mapper}
     * passed as parameter
     *
     * @param mapper {@link Mapper} association to remove
     */
    public void removeCallback(Mapper mapper) {
        callbacks.remove(mapper);
    }

    // This function dispatches the data received from a Channel to the
    // appropriate callback function
    private void asyncDispatch(Optional<Payload> response) {
        if (!response.isPresent() || callbacks.isEmpty()) {
            return;
        }

        for (Mapper mapper : callbacks.keySet()) {
            FpcMessage msg = mapper.unmarshal(response.get());
            if (!msg.validate()) {
                continue;
            }

            AsyncChannelCallback callback = callbacks.get(mapper);
            callback.newMessage(msg);
            break;
        }
    }

    /**
     * Stops the {@link ChannelManager}
     */
    public void stop() {
        callbacks.clear();
        channels.forEach(Channel::close);
    }

    /**
     * Callback interface for the {@link ChannelManager}
     *
     * @author Guido Rota (2015)
     *
     */
    public static interface AsyncChannelCallback {

        /**
         * Invoked when a new message is received asynchronously
         *
         * @param message
         */
        public void newMessage(FpcMessage message);

    }

}
