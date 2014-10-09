package org.dei.perla.channel.http;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.apache.http.entity.ContentType;
import org.dei.perla.channel.http.HttpIORequestDescriptor.HttpMethod;
import org.dei.perla.fpc.descriptor.ChannelDescriptor;
import org.dei.perla.fpc.descriptor.DeviceDescriptor;
import org.dei.perla.fpc.descriptor.IORequestDescriptor;
import org.dei.perla.fpc.descriptor.InvalidDeviceDescriptorException;
import org.dei.perla.utils.Check;
import org.junit.BeforeClass;
import org.junit.Test;

public class HttpRequestBuilderFactoryTest {
	private static DeviceDescriptor device;
	private static HttpIORequestBuilderFactory factory;
	private static Map<HttpMethod, HttpIORequestDescriptor> requestDescriptors;

	private static Map<HttpMethod, HttpIORequestDescriptor> wrongRequestDescriptors;

	private static final String descriptorPath = "src/test/java/org/dei/perla/channel/http/http_descriptor.xml";

	private static final String wrongDescriptorPath = "src/test/java/org/dei/perla/channel/http/wrong_http_descriptor.xml";

	@BeforeClass
	public static void parseDeviceDescriptor() throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("org.dei.perla.fpc.descriptor");
		sb.append(":org.dei.perla.fpc.descriptor.instructions");
		sb.append(":org.dei.perla.message.urlencoded");
		sb.append(":org.dei.perla.message.json");
		sb.append(":org.dei.perla.channel.http");

		JAXBContext jc = JAXBContext.newInstance(sb.toString());

		Unmarshaller unmarshaller = jc.createUnmarshaller();
		StreamSource xml = new StreamSource(descriptorPath);
		device = unmarshaller.unmarshal(xml, DeviceDescriptor.class).getValue();
		factory = new HttpIORequestBuilderFactory();

		List<IORequestDescriptor> requests = device.getRequestList();

		requestDescriptors = new HashMap<>();
		requestDescriptors.put(HttpMethod.GET,
				(HttpIORequestDescriptor) requests.get(0));
		requestDescriptors.put(HttpMethod.POST,
				(HttpIORequestDescriptor) requests.get(1));
		requestDescriptors.put(HttpMethod.PUT,
				(HttpIORequestDescriptor) requests.get(2));
		requestDescriptors.put(HttpMethod.DELETE,
				(HttpIORequestDescriptor) requests.get(3));

		StreamSource wrongXml = new StreamSource(wrongDescriptorPath);
		DeviceDescriptor wrongDevice = unmarshaller.unmarshal(wrongXml,
				DeviceDescriptor.class).getValue();
		factory = new HttpIORequestBuilderFactory();

		List<IORequestDescriptor> wrongRequests = wrongDevice.getRequestList();

		wrongRequestDescriptors = new HashMap<>();
		wrongRequestDescriptors.put(HttpMethod.GET,
				(HttpIORequestDescriptor) wrongRequests.get(0));
		wrongRequestDescriptors.put(HttpMethod.POST,
				(HttpIORequestDescriptor) wrongRequests.get(1));
		wrongRequestDescriptors.put(HttpMethod.PUT,
				(HttpIORequestDescriptor) wrongRequests.get(2));
		wrongRequestDescriptors.put(HttpMethod.DELETE,
				(HttpIORequestDescriptor) wrongRequests.get(3));
	}

	@Test
	public void httpXmlDescriptorConsistency()
			throws InvalidDeviceDescriptorException {
		assertFalse(device.getChannelList().isEmpty());
		ChannelDescriptor channelDescriptor = device.getChannelList().get(0);

		assertTrue(channelDescriptor instanceof HttpChannelDescriptor);
		assertFalse(device.getRequestList().isEmpty());
	}

	public void createGetRequestBuilder()
			throws InvalidDeviceDescriptorException {
		createRequestBuilder(requestDescriptors.get(HttpMethod.GET));
	}

	@Test(expected = InvalidDeviceDescriptorException.class)
	public void createWrongGetRequestBuilder()
			throws InvalidDeviceDescriptorException {
		createRequestBuilder(wrongRequestDescriptors.get(HttpMethod.GET));
	}

	@Test
	public void createPostRequestBuilder()
			throws InvalidDeviceDescriptorException {
		createRequestBuilder(requestDescriptors.get(HttpMethod.POST));
	}

	@Test(expected = InvalidDeviceDescriptorException.class)
	public void createWrongPostRequestBuilder()
			throws InvalidDeviceDescriptorException {
		createRequestBuilder(wrongRequestDescriptors.get(HttpMethod.POST));
	}

	@Test
	public void createPutRequestBuilder()
			throws InvalidDeviceDescriptorException {
		createRequestBuilder(requestDescriptors.get(HttpMethod.PUT));
	}

	@Test(expected = InvalidDeviceDescriptorException.class)
	public void createWrongDeleteRequestBuilder()
			throws InvalidDeviceDescriptorException {
		createRequestBuilder(wrongRequestDescriptors.get(HttpMethod.DELETE));
	}

	@Test
	public void createDeleteRequestBuilder()
			throws InvalidDeviceDescriptorException {
		createRequestBuilder(requestDescriptors.get(HttpMethod.DELETE));
	}

	@Test(expected = InvalidDeviceDescriptorException.class)
	public void createWrongPutRequestBuilder()
			throws InvalidDeviceDescriptorException {
		createRequestBuilder(wrongRequestDescriptors.get(HttpMethod.PUT));
	}

	private void createRequestBuilder(HttpIORequestDescriptor requestDescriptor)
			throws InvalidDeviceDescriptorException {
		HttpIORequest request;
		HttpIORequestBuilder requestBuilder = (HttpIORequestBuilder) factory
				.create(requestDescriptor);

		assertThat(requestBuilder.getRequestId(),
				equalTo(requestDescriptor.getId()));

		request = (HttpIORequest) requestBuilder.create();
		checkRequestWithDescriptor(request, requestDescriptor);
	}

	private void checkRequestWithDescriptor(HttpIORequest request,
			HttpIORequestDescriptor requestDescriptor) {

		assertThat(request.getMethod(), equalTo(requestDescriptor.getMethod()));

		assertThat(request.getUri().toString(),
				equalTo(requestDescriptor.getHost()));

		if (Check.nullOrEmpty(requestDescriptor.getContentType())) {
			if (requestDescriptor.getMethod() == HttpMethod.GET
					|| requestDescriptor.getMethod() == HttpMethod.DELETE) {
				assertNull(request.getContentType());
			} else {
				assertThat(request.getContentType(),
						equalTo(ContentType.WILDCARD));
			}
		} else {
			assertThat(request.getContentType().toString(),
					equalTo(requestDescriptor.getContentType()));
		}

	}

}
