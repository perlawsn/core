package org.dei.perla.core.channel.loopback;

import org.dei.perla.core.channel.AbstractChannel;
import org.dei.perla.core.channel.IORequest;
import org.dei.perla.core.channel.Payload;

public class LoopbackChannel extends AbstractChannel {

	public LoopbackChannel() {
		super("loopback");
	}

	@Override
	public Payload handleRequest(IORequest request)
			throws InterruptedException {
		if (!(request instanceof LoopbackIORequest)) {
			throw new RuntimeException();
		}
		LoopbackIORequest loopReq = (LoopbackIORequest) request;

		return loopReq.getPayload();
	}

}
