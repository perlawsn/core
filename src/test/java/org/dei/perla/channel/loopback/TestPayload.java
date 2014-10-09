package org.dei.perla.channel.loopback;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Map;

import org.dei.perla.channel.Payload;

public class TestPayload implements Payload {

	private final Map<String, Object> valueMap;
	
	protected TestPayload(Map<String, Object> valueMap) {
		this.valueMap = valueMap;
	}
	
	protected Map<String, Object> getValueMap() {
		return valueMap;
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
