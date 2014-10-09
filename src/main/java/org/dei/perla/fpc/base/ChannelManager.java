package org.dei.perla.fpc.base;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.dei.perla.channel.Channel;
import org.dei.perla.channel.IOHandler;
import org.dei.perla.channel.IORequest;
import org.dei.perla.channel.Payload;
import org.dei.perla.message.FpcMessage;
import org.dei.perla.message.Mapper;

public class ChannelManager {

	private static final Logger logger = Logger.getLogger(ChannelManager.class);

	private final List<Channel> channelList;
	private final Map<Mapper, AsyncChannelCallback> callbackMap = new ConcurrentHashMap<>();

	public ChannelManager(List<Channel> channelList) {
		this.channelList = channelList;

		// Set the asynchronous handler on every channel
		IOHandler ioHandler = new IOHandler() {
			@Override
			public void complete(IORequest request, Optional<Payload> result) {
				asyncDispatch(result);
			}

			@Override
			public void error(IORequest request, Throwable cause) {
				logger.warn("Error in asynchronous receive", cause);
			}
		};
		for (Channel channel : channelList) {
			channel.setAsyncIOHandler(ioHandler);
		}
	}

	public void addCallback(Mapper mapper, AsyncChannelCallback callback) {
		callbackMap.put(mapper, callback);
	}

	public void removeCallback(Mapper mapper) {
		callbackMap.remove(mapper);
	}

	private void asyncDispatch(Optional<Payload> response) {
		if (!response.isPresent() || callbackMap.isEmpty()) {
			return;
		}

		for (Mapper mapper : callbackMap.keySet()) {
			FpcMessage msg = mapper.unmarshal(response.get());
			if (!msg.validate()) {
				continue;
			}

			AsyncChannelCallback callback = callbackMap.get(mapper);
			callback.newMessage(msg);
			break;
		}
	}

	public void stop() {
		callbackMap.clear();
		for (Channel channel : channelList) {
			channel.close();
		}
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
