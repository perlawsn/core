package org.dei.perla.core.channel;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * A generic class representing data being sent or received a {@code Channel}.
 *
 * @author Guido Rota (2014)
 *
 */
public interface Payload {

	/**
	 * Returns the charset that can be used to encode or decode the payload. It
	 * defaults to the JVM default charset if no value is specified.
	 *
	 * @return Character set to be used for decoding and encoding the payload
	 *         data.
	 */
	public Charset getCharset();

	/**
	 * Returns an {@code InputStream} that can be used to read the payload data.
	 *
	 * @return An {@code InputStream} that can be used to read the payload data.
	 */
	public InputStream asInputStream();

	/**
	 * Returns a read-only {@code ByteBuffer} that may be used to read the
	 * payload data.
	 *
	 * @return A read-only {@code ByteBuffer} that may be used to read the
	 *         payload data.
	 */
	public ByteBuffer asByteBuffer();

	/**
	 * Returns a string representation of the payload data. The payload is
	 * encoded using the charset associated with this {@code Payload}.
	 *
	 * @return {@code String} representation of the payload data
	 */
	public String asString();

}
