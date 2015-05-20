package org.dei.perla.core.channel;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class LoopbackPayload implements Payload {

	private final String message;

	protected LoopbackPayload(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	@Override
	public Charset getCharset() {
		throw new UnsupportedOperationException();
	}

	@Override
	public InputStream asInputStream() {
		throw new UnsupportedOperationException();
	}

	@Override
	public ByteBuffer asByteBuffer() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String asString() {
		throw new UnsupportedOperationException();
	}

}
