package org.dei.perla.channel;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import org.junit.Test;

public class ByteArrayPayloadTest {

	@Test
	public void defaultCharsetTest() {
		Payload payload = new ByteArrayPayload("test");
		assertThat(payload.getCharset(), equalTo(Charset.defaultCharset()));
	}

	@Test
	public void characterEncoding() {
		String testString = "\u7686\u3055\u3093\u3001\u3053\u3093\u306b\u3061\u306f";
		byte[] payload;

		payload = testString.getBytes(Charset.forName("UTF-16"));
		Payload defaultPayload = new ByteArrayPayload(payload,
				Charset.forName("UTF-16"));
		assertThat(defaultPayload.asString(), equalTo(testString));

		payload = testString.getBytes();
		Payload wrongEncodingPayload = new ByteArrayPayload(
				payload, Charset.forName("ISO-8859-1"));
		assertThat(wrongEncodingPayload.asString(), not(equalTo(testString)));
	}

	@Test
	public void asByteBufferTest() {
		byte[] byteArray = "test_string".getBytes();
		Payload payload = new ByteArrayPayload(byteArray);
		ByteBuffer bbuf = payload.asByteBuffer();
		assertThat(byteArray.length, equalTo(bbuf.limit())); 
		for (int i = 0; i < byteArray.length; i++) {
			assertThat(byteArray[i], equalTo(bbuf.get(i)));
		}
	}
	
	@Test
	public void asInputStream() throws IOException {
		byte[] byteArray = "test_string".getBytes();
		Payload payload = new ByteArrayPayload(byteArray);
		InputStream is = payload.asInputStream();
		for (int i = 0; i < byteArray.length; i++) {
			assertThat(byteArray[i], equalTo((byte) is.read()));
		}
		assertThat(is.read(), equalTo(-1));
	}
	
}
