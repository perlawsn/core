package org.dei.perla.core.channel.http;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import org.dei.perla.core.channel.Payload;

public class StringPayload implements Payload {

	private final String string;

	protected StringPayload(String string) {
		this.string = string;
	}

	@Override
	public Charset getCharset() {
		return null;
	}

	@Override
	public InputStream asInputStream() {
		return null;
	}

	@Override
	public ByteBuffer asByteBuffer() {
		return null;
	}

	@Override
	public String asString() {
		return string;
	}

}
