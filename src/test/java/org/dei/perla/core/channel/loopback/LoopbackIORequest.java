package org.dei.perla.core.channel.loopback;

import org.dei.perla.core.channel.IORequest;
import org.dei.perla.core.channel.Payload;

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
