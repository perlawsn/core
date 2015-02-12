package org.dei.perla.core.message.urlencoded;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.dei.perla.core.channel.ByteArrayPayload;
import org.dei.perla.core.channel.Payload;
import org.dei.perla.core.descriptor.DataType;
import org.dei.perla.core.descriptor.DeviceDescriptor;
import org.dei.perla.core.descriptor.FieldDescriptor;
import org.dei.perla.core.descriptor.InvalidDeviceDescriptorException;
import org.dei.perla.core.message.FpcMessage;
import org.dei.perla.core.message.MapperFactory;
import org.junit.BeforeClass;
import org.junit.Test;

public class UrlEncodedMessageTest {

	private static DeviceDescriptor device;
	private static UrlEncodedMapper mapper = null;
	private static final String descriptor =
            "src/test/java/org/dei/perla/core/message/urlencoded/urlencoded_message_descriptor.xml";

	@BeforeClass
	public static void parseDeviceDescriptor() throws Exception {
		JAXBContext jc = JAXBContext.newInstance("org.dei.perla.core.descriptor"
				+ ":org.dei.perla.core.descriptor.instructions"
				+ ":org.dei.perla.core.message.json"
				+ ":org.dei.perla.core.message.urlencoded"
				+ ":org.dei.perla.core.channel.http");

		Unmarshaller unmarshaller = jc.createUnmarshaller();
		StreamSource xml = new StreamSource(descriptor);
		device = unmarshaller.unmarshal(xml, DeviceDescriptor.class).getValue();

		MapperFactory factory = new UrlEncodedMapperFactory();
		mapper = (UrlEncodedMapper) factory.createMapper(device
				.getMessageList().get(0), null, null);
	}

	@Test
	public void xmlParsing() {
		UrlEncodedMessageDescriptor descriptor;

		assertThat(device.getMessageList().size(), equalTo(1));
		assertThat(
				device.getMessageList().get(0) instanceof UrlEncodedMessageDescriptor,
				equalTo(true));
		descriptor = (UrlEncodedMessageDescriptor) device.getMessageList().get(
				0);
		assertThat(descriptor, notNullValue());
		assertThat(descriptor.getId(), equalTo("urlencoded_message"));
		assertThat(descriptor.getParameterList().size(), equalTo(5));
		assertThat(descriptor.getFieldList().size(), equalTo(5));
	}

	@Test
	public void mapperCreation() throws InvalidDeviceDescriptorException {
		FieldDescriptor field;

		assertThat(mapper, notNullValue());
		assertThat(mapper.getFieldDescriptors(), notNullValue());
		assertThat(mapper.getFieldDescriptors().size(), equalTo(5));

		field = mapper.getFieldDescriptor("temperature");
		assertThat(field, notNullValue());
		assertThat(field.getName(), equalTo("temperature"));
		assertThat(field.getType(), equalTo(DataType.FLOAT.getId()));

		field = mapper.getFieldDescriptor("pressure");
		assertThat(field, notNullValue());
		assertThat(field.getName(), equalTo("pressure"));
		assertThat(field.getType(), equalTo(DataType.FLOAT.getId()));

		field = mapper.getFieldDescriptor("timestamp");
		assertThat(field, notNullValue());
		assertThat(field.getName(), equalTo("timestamp"));
		assertThat(field.getType(), equalTo(DataType.TIMESTAMP.getId()));
		assertThat(field.getFormat(), equalTo("d MMM uuuu HH:mm"));

		field = mapper.getFieldDescriptor("location");
		assertThat(field, notNullValue());
		assertThat(field.getName(), equalTo("location"));

		field = mapper.getFieldDescriptor("key");
		assertThat(field, notNullValue());
		assertThat(field.getName(), equalTo("key"));
		assertTrue(field.isStatic());
		assertThat(field.getValue(), equalTo("5"));

		field = mapper.getFieldDescriptor("fakefield");
		assertThat(field, nullValue());
	}

	@Test
	public void emptyMessage() {
		FpcMessage message = mapper.createMessage();

		assertThat(message.getId(), equalTo("urlencoded_message"));

		assertTrue(message.hasField("temperature"));
		assertTrue(message.hasField("pressure"));
		assertTrue(message.hasField("timestamp"));
		assertTrue(message.hasField("location"));
		assertTrue(message.hasField("key"));
		assertFalse(message.hasField("fakeField"));

		assertThat(message, notNullValue());
		assertThat(message.validate(), equalTo(true));

		message.setField("temperature", 12);
		Object temperature = message.getField("temperature");
		assertThat(temperature, notNullValue());
		assertThat(temperature instanceof Float, equalTo(true));
		assertThat((Float) temperature, equalTo(new Float("12")));

		message.setField("temperature", 21);
		assertThat((Float) message.getField("temperature"), equalTo(new Float(
				"21")));
	}

	@Test(expected = IllegalArgumentException.class)
	public void nonExistentField() {
		FpcMessage message = mapper.createMessage();

		assertThat(message, notNullValue());
		assertThat(message.validate(), equalTo(true));

		message.getField("fake");
	}

	@Test
	public void unmarshalCorrect() {
		Payload payload = new ByteArrayPayload(
				"temperature=43&pressure=567&key=5&location=milan&timestamp=3%20Jun%202014%2014:35");
		FpcMessage message = mapper.unmarshal(payload);

		assertThat(message, notNullValue());
		assertThat(message.validate(), equalTo(true));
		assertThat((Float) message.getField("temperature"),
				equalTo(43f));
		assertThat((Float) message.getField("pressure"), equalTo(567f));
	}

	@Test
	public void unmarshalWrong() {
		Payload payload = new ByteArrayPayload(
				"temperature=43&pressure=567&key=3&location=milan&time=now");
		FpcMessage message = mapper.unmarshal(payload);

		assertThat(message, notNullValue());
		assertThat(message.validate(), equalTo(false));
		assertThat((Float) message.getField("temperature"),
				equalTo(43f));
		assertThat((Float) message.getField("pressure"), equalTo(567f));
	}

	@Test
	public void marshal() {
		FpcMessage message = mapper.createMessage();

		ZonedDateTime now = ZonedDateTime.now();
		String nowString = now.format(DateTimeFormatter
				.ofPattern("d MMM uuuu HH:mm"));

		message.setField("temperature", 45);
		message.setField("pressure", 874);
		message.setField("timestamp", Instant.from(now));
		assertThat(message.validate(), equalTo(true));
		Payload payload = mapper.marshal(message);

		List<? extends NameValuePair> parameterList = URLEncodedUtils.parse(
				payload.asString(), payload.getCharset());
		boolean temperatureChecked = false;
		boolean pressureChecked = false;
		boolean timestampChecked = false;
		for (NameValuePair parameter : parameterList) {
			if (parameter.getName().equals("temperature")) {
				assertThat(new Float(parameter.getValue()), equalTo(new Float(
						"45")));
				temperatureChecked = true;

			} else if (parameter.getName().equals("pressure")) {
				assertThat(new Float(parameter.getValue()), equalTo(new Float(
						"874")));
				pressureChecked = true;
			} else if (parameter.getName().equals("timestamp")) {
				assertThat(parameter.getValue(), equalTo(nowString));
				timestampChecked = true;
			}
		}
		assertThat(temperatureChecked, equalTo(true));
		assertThat(pressureChecked, equalTo(true));
		assertThat(timestampChecked, equalTo(true));
	}

}
