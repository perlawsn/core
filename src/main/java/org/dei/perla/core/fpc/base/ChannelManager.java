package org.dei.perla.core.fpc.base;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.dei.perla.core.channel.Channel;
import org.dei.perla.core.channel.IOHandler;
import org.dei.perla.core.channel.IORequest;
import org.dei.perla.core.channel.Payload;
import org.dei.perla.core.message.FpcMessage;
import org.dei.perla.core.message.Mapper;

public class ChannelManager {

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

	public void addCallback(Mapper mapper, AsyncChannelCallback callback) {
		callbacks.put(mapper, callback);
	}

	public void removeCallback(Mapper mapper) {
		callbacks.remove(mapper);
	}

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

	public void stop() {
		callbacks.clear();
        channels.forEach(Channel::close);
	}

	public static interface AsyncChannelCallback {

		/**
		 * Invoked when a new message is received asynchronously
		 *
		 * @param message
		 */
		public void newMessage(FpcMessage message);

	}

}
