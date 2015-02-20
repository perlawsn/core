package org.dei.perla.core.message.json;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import java.io.File;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javassist.ClassPool;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.FileUtils;
import org.dei.perla.core.channel.ByteArrayPayload;
import org.dei.perla.core.channel.Payload;
import org.dei.perla.core.descriptor.AttributeDescriptor;
import org.dei.perla.core.descriptor.DeviceDescriptor;
import org.dei.perla.core.descriptor.FieldDescriptor;
import org.dei.perla.core.descriptor.InvalidDeviceDescriptorException;
import org.dei.perla.core.message.FpcMessage;
import org.dei.perla.core.message.Mapper;
import org.junit.BeforeClass;
import org.junit.Test;

public class JsonMessageTest {

	private static final Map<String, AttributeDescriptor> attributeMap = new HashMap<>();
	private static DeviceDescriptor device;
	private static Payload envJson;
	private static Payload fullJson;
	private static Payload wrongJson;
	private static JsonMapper envMapper = null;
	private static JsonMapper roomMapper = null;
	private static final String descriptor =
            "src/test/java/org/dei/perla/core/message/json/json_message_descriptor.xml";

	@BeforeClass
	public static void parseDeviceDescriptor() throws Exception {
		JAXBContext jc = JAXBContext
				.newInstance("org.dei.perla.core.descriptor"
						+ ":org.dei.perla.core.descriptor.instructions"
						+ ":org.dei.perla.core.message.json"
						+ ":org.dei.perla.core.channel.http");

		Unmarshaller unmarshaller = jc.createUnmarshaller();
		StreamSource xml = new StreamSource(descriptor);
		device = unmarshaller.unmarshal(xml, DeviceDescriptor.class).getValue();

		for (AttributeDescriptor attribute : device.getAttributeList()) {
			attributeMap.put(attribute.getId(), attribute);
		}

		byte[] envJsonByte = FileUtils.readFileToByteArray(new File(
				"src/test/java/org/dei/perla/core/message/json/env_test.json"));
		envJson = new ByteArrayPayload(envJsonByte);

		byte[] fullJsonByte = FileUtils.readFileToByteArray(new File(
				"src/test/java/org/dei/perla/core/message/json/json_test.json"));
		fullJson = new ByteArrayPayload(fullJsonByte);
		byte[] wrongJsonByte = FileUtils
				.readFileToByteArray(new File(
						"src/test/java/org/dei/perla/core/message/json/wrong_json_test.json"));
		wrongJson = new ByteArrayPayload(wrongJsonByte);

		ClassPool classPool = new ClassPool(true);
		Map<String, Mapper> mapperMap = new HashMap<>();
		JsonMapperFactory factory = new JsonMapperFactory();
		envMapper = (JsonMapper) factory.createMapper(device.getMessageList()
				.get(0), mapperMap, classPool);
		mapperMap.put(envMapper.getMessageId(), envMapper);
		roomMapper = (JsonMapper) factory.createMapper(device.getMessageList()
				.get(1), mapperMap, classPool);
	}

	@Test
	public void mapperCreationTest() throws InvalidDeviceDescriptorException {
		FieldDescriptor field;
		assertThat(device.getMessageList().size(), equalTo(2));

		// environment message
		JsonObjectDescriptor envDesc = (JsonObjectDescriptor) device
				.getMessageList().get(0);

		assertThat(envDesc.getFieldList().size(), equalTo(4));
		assertThat(envMapper, notNullValue());
		assertThat(envMapper.getFieldDescriptors(), notNullValue());
		assertThat(envMapper.getFieldDescriptors().size(), equalTo(4));

		field = envMapper.getFieldDescriptor("temperature");
		assertThat(field, notNullValue());
		assertThat(field.getName(), equalTo("temperature"));

		field = envMapper.getFieldDescriptor("pressure");
		assertThat(field, notNullValue());
		assertThat(field.getName(), equalTo("pressure"));

		field = envMapper.getFieldDescriptor("light");
		assertThat(field, notNullValue());
		assertThat(field.getName(), equalTo("light"));

		field = envMapper.getFieldDescriptor("gravity");
		assertThat(field, notNullValue());
		assertThat(field.getName(), equalTo("gravity"));

		field = envMapper.getFieldDescriptor("name");
		assertThat(field, nullValue());

		// room message
		JsonObjectDescriptor roomDesc = (JsonObjectDescriptor) device
				.getMessageList().get(1);

		assertThat(roomDesc.getFieldList().size(), equalTo(5));
		assertThat(roomMapper, notNullValue());
		assertThat(roomMapper.getFieldDescriptors(), notNullValue());
		assertThat(roomMapper.getFieldDescriptors().size(), equalTo(5));

		field = roomMapper.getFieldDescriptor("name");
		assertThat(field, notNullValue());
		assertThat(field.getName(), equalTo("name"));

		field = roomMapper.getFieldDescriptor("time");
		assertThat(field, notNullValue());
		assertThat(field.getName(), equalTo("time"));

		field = roomMapper.getFieldDescriptor("number");
		assertThat(field, notNullValue());
		assertThat(field.getName(), equalTo("number"));

		field = roomMapper.getFieldDescriptor("environment");
		assertThat(field, notNullValue());
		assertThat(field.getName(), equalTo("environment"));

		field = roomMapper.getFieldDescriptor("occupants");
		assertThat(field, notNullValue());
		assertThat(field.getName(), equalTo("occupants"));

		field = roomMapper.getFieldDescriptor("light");
		assertThat(field, nullValue());
	}

	@Test
	public void getIdTest() {
		FpcMessage room = roomMapper.createMessage();
		assertThat(room.getId(), equalTo("room"));

		FpcMessage env = envMapper.createMessage();
		assertThat(env.getId(), equalTo("environment"));
	}

	@Test
	public void hasFieldTest() {
		FpcMessage room = roomMapper.createMessage();
		assertTrue(room.hasField("name"));
		assertTrue(room.hasField("time"));
		assertTrue(room.hasField("number"));

		FpcMessage env = envMapper.createMessage();
		assertTrue(env.hasField("temperature"));
		assertTrue(env.hasField("pressure"));
		assertTrue(env.hasField("light"));
		assertTrue(env.hasField("gravity"));
	}

	@Test
	public void getEnvironmentFieldTest() {
		FpcMessage env = envMapper.unmarshal(envJson);

		Object temp = env.getField("temperature");
		assertThat(temp, notNullValue());
		assertTrue(temp instanceof Float);
		assertThat((Float) temp, equalTo(21f));

		Object press = env.getField("pressure");
		assertThat(press, notNullValue());
		assertTrue(press instanceof Float);
		assertThat((Float) press, equalTo(234f));

		Object light = env.getField("light");
		assertThat(light, notNullValue());
		assertTrue(light instanceof Integer);
		assertThat((Integer) light, equalTo(12));

		Object gravity = env.getField("gravity");
		assertThat(gravity, notNullValue());
		assertTrue(gravity instanceof Float);
		assertThat((Float) gravity, equalTo(9.18f));
	}

	@Test
	public void getRoomFieldTest() {
		FpcMessage room = roomMapper.unmarshal(fullJson);

		Object name = room.getField("name");
		assertThat(name, notNullValue());
		assertTrue(name instanceof String);
		assertThat((String) name, equalTo("test_room"));

		Object number = room.getField("number");
		assertThat(number, notNullValue());
		assertTrue(number instanceof Integer);
		assertThat((Integer) number, equalTo(12));

		Object environment = room.getField("environment");
		assertThat(environment, notNullValue());
		assertTrue(environment instanceof FpcMessage);
		FpcMessage env = (FpcMessage) environment;

		Object time = room.getField("time");
		assertThat(time, notNullValue());
		assertTrue(time instanceof Instant);
		ZonedDateTime t = ((Instant) time).atZone(ZoneId.systemDefault());
		assertThat(t.get(ChronoField.YEAR), equalTo(2014));
		assertThat(t.get(ChronoField.MONTH_OF_YEAR), equalTo(6));
		assertThat(t.get(ChronoField.DAY_OF_MONTH), equalTo(3));
		assertThat(t.get(ChronoField.HOUR_OF_DAY), equalTo(14));
		assertThat(t.get(ChronoField.MINUTE_OF_HOUR), equalTo(35));

		Object temp = env.getField("temperature");
		assertThat(temp, notNullValue());
		assertTrue(temp instanceof Float);
		assertThat((Float) temp, equalTo(21f));

		Object press = env.getField("pressure");
		assertThat(press, notNullValue());
		assertTrue(press instanceof Float);
		assertThat((Float) press, equalTo(234f));

		Object light = env.getField("light");
		assertThat(light, notNullValue());
		assertTrue(light instanceof Integer);
		assertThat((Integer) light, equalTo(12));

		Object gravity = env.getField("gravity");
		assertThat(gravity, notNullValue());
		assertTrue(gravity instanceof Float);
		assertThat((Float) gravity, equalTo(9.18f));

		Object occupants = room.getField("occupants");
		assertThat(occupants, notNullValue());
		assertTrue(occupants instanceof ArrayList<?>);
		ArrayList<?> o = (ArrayList<?>) occupants;
		assertThat(o.size(), equalTo(2));
		assertTrue(o.contains("Mario"));
		assertTrue(o.contains("Gianni"));
	}

	@Test
	public void setFieldTest() {
		FpcMessage env = envMapper.unmarshal(envJson);

		assertThat((Float) env.getField("temperature"), equalTo(21f));
		env.setField("temperature", 12f);
		assertThat((Float) env.getField("temperature"), equalTo(12f));
	}

	@Test
	public void appendElementTest() {
		FpcMessage room = roomMapper.createMessage();

		ArrayList<?> occupants = (ArrayList<?>) room.getField("occupants");
		assertThat(occupants.size(), equalTo(0));
		room.appendElement("occupants", "test");
		assertThat(occupants.size(), equalTo(1));
	}

	@Test(expected = IllegalArgumentException.class)
	public void getNonExistentField() {
		FpcMessage env = envMapper.unmarshal(envJson);

		env.getField("fake");
	}

	@Test(expected = IllegalArgumentException.class)
	public void setNonExistentField() {
		FpcMessage env = envMapper.unmarshal(envJson);

		env.setField("fake", 12);
	}

	@Test
	public void validationTest() {
		FpcMessage room = roomMapper.unmarshal(fullJson);
		assertTrue(room.validate());

		FpcMessage wrongUnmarshalled = roomMapper.unmarshal(wrongJson);
		assertFalse(wrongUnmarshalled.validate());
	}

}
