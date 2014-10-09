package org.dei.perla.channel.loopback;

import org.dei.perla.channel.AbstractChannel;
import org.dei.perla.channel.IORequest;
import org.dei.perla.channel.Payload;

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
