package org.dei.perla.core.channel.simulator;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.dei.perla.core.channel.Payload;
import org.dei.perla.core.fpc.descriptor.AttributeDescriptor;
import org.dei.perla.core.fpc.descriptor.DeviceDescriptor;
import org.dei.perla.core.fpc.descriptor.MessageDescriptor;
import org.dei.perla.core.message.FpcMessage;
import org.dei.perla.core.message.Mapper;
import org.dei.perla.core.message.MapperFactory;
import org.junit.BeforeClass;
import org.junit.Test;

public class SimulatorMessageTest {

	private static DeviceDescriptor device;
	private static final Map<String, AttributeDescriptor> attributeMap = new HashMap<>();
	private static final String descriptorPath =
            "src/test/java/org/dei/perla/core/channel/simulator/simulator_descriptor.xml";

	@BeforeClass
	public static void parseDeviceDescriptor() throws Exception {
		JAXBContext jc = JAXBContext.newInstance("org.dei.perla.core.fpc.descriptor"
				+ ":org.dei.perla.core.fpc.descriptor.instructions"
				+ ":org.dei.perla.core.channel.simulator");

		Unmarshaller unmarshaller = jc.createUnmarshaller();
		StreamSource xml = new StreamSource(descriptorPath);
		device = unmarshaller.unmarshal(xml, DeviceDescriptor.class).getValue();

		for (AttributeDescriptor attribute : device.getAttributeList()) {
			attributeMap.put(attribute.getId(), attribute);
		}
	}

	@Test
	public void parseMessageTest() {

		assertThat(device, notNullValue());
		assertThat(device.getMessageList().size(), equalTo(4));

		for (MessageDescriptor descriptor : device.getMessageList()) {
			assertTrue(descriptor instanceof SimulatorMessageDescriptor);
			switch (descriptor.getId()) {
			case "sampling-period":
				assertThat(descriptor.getFieldList().size(), equalTo(1));
				break;
			case "temp-only":
				assertThat(descriptor.getFieldList().size(), equalTo(2));
				break;
			case "press-only":
				assertThat(descriptor.getFieldList().size(), equalTo(2));
				break;
			case "all":
				assertThat(descriptor.getFieldList().size(), equalTo(4));
				break;
			default:
				throw new RuntimeException();
			}
		}
	}

	@Test
	public void simulatorPayloadTest() {
		Map<String, Object> valueMap = new HashMap<>();

		valueMap.put("one", "one");
		valueMap.put("two", 2);
		valueMap.put("three", 3.0);

		SimulatorPayload payload = new SimulatorPayload(valueMap);
		assertThat(payload, notNullValue());
		assertThat(payload.getValueMap(), notNullValue());

		for (Entry<String, Object> entry : payload.getValueMap().entrySet()) {
			assertTrue(valueMap.containsKey(entry.getKey()));
			assertThat(valueMap.get(entry.getKey()), equalTo(entry.getValue()));
		}
	}

	@Test
	public void simulatorMapperFactoryTest() throws Exception {
		MapperFactory factory = new SimulatorMapperFactory();

		for (MessageDescriptor message : device.getMessageList()) {
			Mapper mapper = factory.createMapper(message, null, null);
			assertThat(mapper, notNullValue());
			assertThat(mapper.getMessageId(), equalTo(message.getId()));

			switch (mapper.getMessageId()) {
			case "temp-only":
				assertThat(mapper.getFieldDescriptors().size(), equalTo(2));
				break;
			case "press-only":
				assertThat(mapper.getFieldDescriptors().size(), equalTo(2));
				break;
			case "all":
				assertThat(mapper.getFieldDescriptors().size(), equalTo(4));
				break;
			case "sampling-period":
				assertThat(mapper.getFieldDescriptors().size(), equalTo(1));
				break;
			default:
				throw new RuntimeException();
			}
		}
	}

	@Test
	public void simulatorMapperUnmarshalTest() throws Exception {
		MapperFactory factory = new SimulatorMapperFactory();
		Map<String, Object> correctValueMap = new HashMap<>();
		Map<String, Object> wrongValueMap = new HashMap<>();

		correctValueMap.put("temperature", 25f);
		correctValueMap.put("pressure", 534f);
		correctValueMap.put("type", "all");
		Payload correctPayload = new SimulatorPayload(correctValueMap);

		wrongValueMap.put("temperature", 25f);
		wrongValueMap.put("pressure", 534f);
		wrongValueMap.put("type", "error");
		Payload wrongPayload = new SimulatorPayload(wrongValueMap);

		Mapper mapper = factory.createMapper(device.getMessageList().get(2),
				null, null);
		assertThat(mapper, notNullValue());

		FpcMessage correctMessage = mapper.unmarshal(correctPayload);
		assertThat(correctMessage, notNullValue());
		assertTrue(correctMessage instanceof SimulatorMessage);
		assertThat((Float) correctMessage.getField("temperature"), equalTo(25f));
		assertThat((Float) correctMessage.getField("pressure"), equalTo(534f));
		assertTrue(correctMessage.validate());

		FpcMessage wrongMessage = mapper.unmarshal(wrongPayload);
		assertThat(wrongMessage, notNullValue());
		assertTrue(wrongMessage instanceof SimulatorMessage);
		assertThat((Float) wrongMessage.getField("temperature"), equalTo(25f));
		assertThat((Float) wrongMessage.getField("pressure"), equalTo(534f));
		assertFalse(wrongMessage.validate());
	}

	@Test
	public void SimulatorMapperMarshalTest() throws Exception {
		MapperFactory factory = new SimulatorMapperFactory();

		Mapper mapper = factory.createMapper(device.getMessageList().get(2),
				null, null);
		assertThat(mapper, notNullValue());

		FpcMessage message = mapper.createMessage();
		message.setField("temperature", 2.4f);
		message.setField("pressure", 455f);
		Payload marshalled = mapper.marshal(message);
		assertThat(marshalled, notNullValue());
		assertTrue(marshalled instanceof SimulatorPayload);
		Map<String, Object> unmarshalledMap = ((SimulatorPayload) marshalled)
				.getValueMap();
		for (Entry<String, Object> entry : unmarshalledMap.entrySet()) {
			assertThat(entry.getValue(),
					equalTo(message.getField(entry.getKey())));
		}
	}

}
