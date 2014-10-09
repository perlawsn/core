package org.dei.perla.channel.http;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.apache.http.entity.ContentType;
import org.dei.perla.channel.http.HttpIORequestDescriptor.HttpMethod;
import org.dei.perla.fpc.descriptor.ChannelDescriptor;
import org.dei.perla.fpc.descriptor.DeviceDescriptor;
import org.dei.perla.utils.Check;
import org.junit.BeforeClass;
import org.junit.Test;

public class HttpChannelDescriptorTest {
	private static DeviceDescriptor device;

	private static final String descriptorPath = "src/test/java/org/dei/perla/channel/http/http_descriptor.xml";

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
	}

	@Test
	public void httpChannelDescriptorConsistency() {
		ChannelDescriptor channel = device.getChannelList().get(0);
		assertTrue(channel instanceof HttpChannelDescriptor);

	}

	@Test
	public void httpGetRequestDescriptorConsistency() {
		HttpIORequestDescriptor request = (HttpIORequestDescriptor) device
				.getRequestList().get(0);

		assertThat(request.getMethod(), equalTo(HttpMethod.GET));
		assertTrue(!Check.nullOrEmpty(request.getId()));
		assertTrue(!Check.nullOrEmpty(request.getHost()));
		assertTrue(request.getContentType() == null);
	}

	@Test
	public void httpPostRequestDescriptorConsistency() {
		HttpIORequestDescriptor request = (HttpIORequestDescriptor) device
				.getRequestList().get(1);

		assertThat(request.getMethod(), equalTo(HttpMethod.POST));
		assertTrue(!Check.nullOrEmpty(request.getId()));
		assertTrue(!Check.nullOrEmpty(request.getHost()));

		if (!Check.nullOrEmpty(request.getContentType())) {
			ContentType ct = ContentType.parse(request.getContentType());
			assertThat(ct.toString(), equalTo(request.getContentType()));
		}
	}
	
	@Test
	public void httpPutRequestDescriptorConsistency() {
		HttpIORequestDescriptor request = (HttpIORequestDescriptor) device
				.getRequestList().get(2);

		assertThat(request.getMethod(), equalTo(HttpMethod.PUT));
		assertTrue(!Check.nullOrEmpty(request.getId()));
		assertTrue(!Check.nullOrEmpty(request.getHost()));

		if (!Check.nullOrEmpty(request.getContentType())) {
			ContentType ct = ContentType.parse(request.getContentType());
			assertThat(ct.toString(), equalTo(request.getContentType()));
		}
	}
	
	@Test
	public void httpDeleteRequestDescriptorConsistency() {
		HttpIORequestDescriptor request = (HttpIORequestDescriptor) device
				.getRequestList().get(3);

		assertThat(request.getMethod(), equalTo(HttpMethod.DELETE));
		assertTrue(!Check.nullOrEmpty(request.getId()));
		assertTrue(!Check.nullOrEmpty(request.getHost()));
		assertTrue(request.getContentType() == null);
	}
}
