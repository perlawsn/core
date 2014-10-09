package org.dei.perla.channel;

import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * A basic {@code Payload} implementation that uses a byte array to store the
 * payload data.
 * 
 * @author Guido Rota (2014)
 * 
 */
public class ByteArrayPayload implements Payload {

	private byte[] byteArray;
	private Charset charset = Charset.defaultCharset();

	/**
	 * Creates a new {@code ByteArrayPayload} that contains a copy of the byte
	 * array passed as parameter. This constructor assumes that the byte array
	 * is encodd using the JVM default character set.
	 * 
	 * @param payload
	 *            Byte array to be copied inside this {@code Payload}
	 */
	public ByteArrayPayload(byte[] payload) {
		this.byteArray = Arrays.copyOf(payload, payload.length);
	}

	/**
	 * Creates a new {@code ByteArrayPayload} that contains a copy of the byte
	 * array passed as parameter. This constructor allows the caller to
	 * explicitly specify the character encoding of the payload.
	 * 
	 * @param payload
	 *            Byte array to be copied inside this {@code Payload}
	 * @param charset
	 *            Charset of the payload
	 */
	public ByteArrayPayload(byte[] payload, Charset charset) {
		this(payload);
		this.charset = charset;
	}

	/**
	 * Creaes a new {@code ByteArrayPayload} that contains a copy of the String
	 * passed as parameter. This constructor assumes that the String is encoded
	 * according to the JVM default character set.
	 * 
	 * @param payload
	 *            String payload
	 */
	public ByteArrayPayload(String payload) {
		byteArray = payload.getBytes();
	}

	/**
	 * Creaes a new {@code ByteArrayPayload} that contains a copy of the String
	 * passed as parameter. This constructor allows the caller to explicitly
	 * specify the character encoding of the payload.
	 * 
	 * @param payload
	 *            String payload
	 * @param charset
	 *            Charset of the payload
	 */
	public ByteArrayPayload(String payload, Charset charset) {
		byteArray = payload.getBytes(charset);
		this.charset = charset;
	}

	@Override
	public Charset getCharset() {
		return charset;
	}

	@Override
	public ByteArrayInputStream asInputStream() {
		return new ByteArrayInputStream(byteArray);
	}

	@Override
	public ByteBuffer asByteBuffer() {
		return ByteBuffer.wrap(byteArray).asReadOnlyBuffer();
	}

	@Override
	public String asString() {
		return new String(byteArray, charset);
	}

}
