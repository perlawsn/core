package org.dei.perla.core.channel;

public class LoopbackRequest implements IORequest {

	private String message;

	protected LoopbackRequest(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	@Override
	public String getId() {
		return "test";
	}

	@Override
	public void setParameter(String name, Payload payload) {
	}

}
