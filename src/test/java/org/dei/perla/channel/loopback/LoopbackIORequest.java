package org.dei.perla.channel.loopback;

import org.dei.perla.channel.IORequest;
import org.dei.perla.channel.Payload;

public class LoopbackIORequest implements IORequest {

	private Payload payload;

	@Override
	public String getId() {
		return "loopback-request";
	}

	@Override
	public void setParameter(String name, Payload payload) {
		this.payload = payload;
	}

	protected Payload getPayload() {
		return payload;
	}

}
