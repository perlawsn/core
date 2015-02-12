package org.dei.perla.core.channel.loopback;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.dei.perla.core.channel.Channel;
import org.dei.perla.core.channel.Payload;
import org.dei.perla.core.channel.IORequest;
import org.dei.perla.core.channel.IORequestBuilder;
import org.dei.perla.core.channel.SynchronizerIOHandler;
import org.dei.perla.core.message.FpcMessage;
import org.dei.perla.core.message.Mapper;
import org.junit.Test;

public class LoopbackChannelTest {

	@Test
	public void testMapper() {
		Mapper mapper = new TestMapper("test");
		FpcMessage msg = mapper.createMessage();
		assertThat(msg, notNullValue());
		assertThat(msg.getId(), equalTo("test"));

		msg.setField("att1", 5);
		msg.setField("att2", "test");

		Object value = msg.getField("att1");
		assertThat(value, notNullValue());
		assertTrue(value instanceof Integer);
		assertThat((Integer) value, equalTo(5));

		value = msg.getField("att2");
		assertThat(value, notNullValue());
		assertTrue(value instanceof String);
		assertThat((String) value, equalTo("test"));

		Payload payload = mapper.marshal(msg);
		assertThat(payload, notNullValue());
		assertTrue(payload instanceof TestPayload);

		FpcMessage unmarshalled = mapper.unmarshal(payload);
		value = unmarshalled.getField("att1");
		assertThat(value, notNullValue());
		assertTrue(value instanceof Integer);
		assertThat((Integer) value, equalTo(5));

		value = unmarshalled.getField("att2");
		assertThat(value, notNullValue());
		assertTrue(value instanceof String);
		assertThat((String) value, equalTo("test"));
	}

	@Test
	public void testLoopbackChannel() throws Exception {
		Mapper mapper = new TestMapper("test");
		FpcMessage msg = mapper.createMessage();
		Channel channel = new LoopbackChannel();
		IORequestBuilder builder = new LoopbackIORequestBuilder("test");

		msg.setField("att1", 5);
		msg.setField("att2", "test");

		IORequest req = builder.create();
		req.setParameter("test", mapper.marshal(msg));

		SynchronizerIOHandler syncHandler = new SynchronizerIOHandler();
		channel.submit(req, syncHandler);
		Payload recvdPayload = syncHandler.getResult().orElseThrow(
				RuntimeException::new);
		assertThat(recvdPayload, notNullValue());

		FpcMessage recvdMsg = mapper.unmarshal(recvdPayload);
		Object value = recvdMsg.getField("att1");
		assertThat(value, notNullValue());
		assertTrue(value instanceof Integer);
		assertThat((Integer) value, equalTo(5));

		value = recvdMsg.getField("att2");
		assertThat(value, notNullValue());
		assertTrue(value instanceof String);
		assertThat((String) value, equalTo("test"));
	}

}
