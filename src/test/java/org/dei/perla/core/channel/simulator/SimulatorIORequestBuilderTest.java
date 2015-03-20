package org.dei.perla.core.channel.simulator;

import org.dei.perla.core.channel.IORequest;
import org.dei.perla.core.channel.IORequestBuilder;
import org.dei.perla.core.channel.IORequestBuilder.IORequestParameter;
import org.dei.perla.core.channel.IORequestBuilderFactory;
import org.dei.perla.core.descriptor.DeviceDescriptor;
import org.dei.perla.core.descriptor.IORequestDescriptor;
import org.dei.perla.core.descriptor.InvalidDeviceDescriptorException;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.*;

public class SimulatorIORequestBuilderTest {

	private static DeviceDescriptor device;
	private static final String descriptorPath =
            "src/test/java/org/dei/perla/core/channel/simulator/simulator_descriptor.xml";

	@BeforeClass
	public static void parseDeviceDescriptor() throws Exception {
		JAXBContext jc = JAXBContext.newInstance("org.dei.perla.core.descriptor"
				+ ":org.dei.perla.core.descriptor.instructions"
				+ ":org.dei.perla.core.channel.simulator");

		Unmarshaller unmarshaller = jc.createUnmarshaller();
		StreamSource xml = new StreamSource(descriptorPath);
		device = unmarshaller.unmarshal(xml, DeviceDescriptor.class).getValue();
	}

	@Test
	public void parseRequestTest() {
		assertThat(device.getRequestList(), notNullValue());
		assertThat(device.getRequestList().size(), equalTo(4));
		for (IORequestDescriptor req : device.getRequestList()) {
			assertTrue(req instanceof SimulatorIORequestDescriptor);
			SimulatorIORequestDescriptor simReq = (SimulatorIORequestDescriptor) req;
			switch (req.getId()) {
			case "temp-only":
				assertThat(simReq.getGeneratorId(), equalTo("temp-only"));
				break;
			case "press-only":
				assertThat(simReq.getGeneratorId(), equalTo("press-only"));
				break;
			case "all":
				assertThat(simReq.getGeneratorId(), equalTo("all"));
				break;
			case "temp-periodic":
				assertThat(simReq.getGeneratorId(), equalTo("temp-only"));
				break;
			default:
				throw new RuntimeException("Unexpected request id "
						+ req.getId());
			}
		}
	}

	@Test
	public void createRequestBuilderTest()
			throws InvalidDeviceDescriptorException {
		IORequestBuilderFactory factory = new SimulatorIORequestBuilderFactory();
		for (IORequestDescriptor desc : device.getRequestList()) {
			IORequestBuilder builder = factory.create(desc);
			assertThat(builder, notNullValue());
			assertTrue(builder instanceof SimulatorIORequestBuilder);
			SimulatorIORequestBuilder simBuilder = (SimulatorIORequestBuilder) builder;

			assertThat(simBuilder.getParameterList(), notNullValue());
			assertThat(simBuilder.getParameterList().size(), equalTo(1));
			IORequestParameter param = simBuilder.getParameterList().get(0);
			assertThat(param, notNullValue());
			assertThat(param.getName(), equalTo("period"));
			assertFalse(param.isMandatory());

			IORequest req = builder.create();
			assertThat(req, notNullValue());
			assertTrue(req instanceof SimulatorIORequest);
			SimulatorIORequest simReq = (SimulatorIORequest) req;

			switch (desc.getId()) {
			case "temp-only":
				assertThat(simBuilder.getRequestId(), equalTo("temp-only"));
				assertThat(simReq.getGeneratorId(), equalTo("temp-only"));
				break;
			case "press-only":
				assertThat(simBuilder.getRequestId(), equalTo("press-only"));
				assertThat(simReq.getGeneratorId(), equalTo("press-only"));
				break;
			case "all":
				assertThat(simBuilder.getRequestId(), equalTo("all"));
				assertThat(simReq.getGeneratorId(), equalTo("all"));
				break;
			case "temp-periodic":
				assertThat(simBuilder.getRequestId(), equalTo("temp-periodic"));
				assertThat(simReq.getGeneratorId(), equalTo("temp-only"));
				break;
			default:
				throw new RuntimeException("Unexpected request id "
						+ desc.getId());
			}
		}
	}

}
