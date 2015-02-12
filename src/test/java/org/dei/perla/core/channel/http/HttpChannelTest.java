package org.dei.perla.core.channel.http;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.entity.ContentType;
import org.dei.perla.core.channel.Channel;
import org.dei.perla.core.channel.ChannelException;
import org.dei.perla.core.channel.ChannelFactory;
import org.dei.perla.core.channel.Payload;
import org.dei.perla.core.channel.SynchronizerIOHandler;
import org.dei.perla.core.channel.http.HttpIORequestDescriptor.HttpMethod;
import org.dei.perla.core.descriptor.InvalidDeviceDescriptorException;
import org.junit.BeforeClass;
import org.junit.Test;

public class HttpChannelTest {

	private static ChannelFactory channelFactory;

	@BeforeClass
	public static void setupTest() {
		channelFactory = new HttpChannelFactory();
	}

	@Test
	public void startupShutdownTest() throws InvalidDeviceDescriptorException {
		Channel channel = channelFactory
				.createChannel(new HttpChannelDescriptor());
		assertNotNull(channel);
		assertFalse(channel.isClosed());
		channel.close();
		assertTrue(channel.isClosed());
	}

	@Test
	public void testGet() throws Exception {
		Channel channel = channelFactory
				.createChannel(new HttpChannelDescriptor());
		HttpIORequest request;

		URL descriptorUrl = new URL("http://www.google.com");
		String requestId = "req-test-google";
		request = new HttpIORequest(requestId, HttpMethod.GET, descriptorUrl,
				null);

		SynchronizerIOHandler syncHandler = new SynchronizerIOHandler();
		channel.submit(request, syncHandler);
		Payload response = syncHandler.getResult().orElseThrow(
				RuntimeException::new);
		assertNotNull(response);

		channel.close();
		assertTrue(channel.isClosed());
	}

	@Test
	public void testPost() throws Exception {
		Channel channel = channelFactory
				.createChannel(new HttpChannelDescriptor());
		HttpIORequest request;

		URL descriptorUrl = new URL("http://posttestserver.com/post.php");
		String requestId = "req-test-google";
		request = new HttpIORequest(requestId, HttpMethod.POST, descriptorUrl,
				ContentType.APPLICATION_FORM_URLENCODED);
		request.setParameter(HttpIORequest.NamedParameterKey.ENTITY,
				new StringPayload("q=PerLa"));

		SynchronizerIOHandler syncHandler = new SynchronizerIOHandler();
		channel.submit(request, syncHandler);
		Payload response = syncHandler.getResult().orElseThrow(
				RuntimeException::new);
		assertNotNull(response);

		channel.close();
		assertTrue(channel.isClosed());
		assertTrue(response.asString().startsWith(
				"Successfully dumped 1 post variables."));
	}

	@Test(expected = ChannelException.class)
	public void requestWhenChannelClosed()
			throws InvalidDeviceDescriptorException, MalformedURLException {
		Channel channel = channelFactory
				.createChannel(new HttpChannelDescriptor());
		HttpIORequest request;

		URL descriptorUrl = new URL("http://www.google.com");
		String requestId = "req-test-google";
		request = new HttpIORequest(requestId, HttpMethod.GET, descriptorUrl,
				null);

		channel.close();
		assertTrue(channel.isClosed());
		SynchronizerIOHandler syncHandler = new SynchronizerIOHandler();
		channel.submit(request, syncHandler);
	}

}
