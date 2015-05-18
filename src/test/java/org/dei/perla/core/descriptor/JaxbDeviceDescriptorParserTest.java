package org.dei.perla.core.descriptor;

import org.dei.perla.core.channel.simulator.SimulatorChannelDescriptor;
import org.dei.perla.core.channel.simulator.SimulatorIORequestDescriptor;
import org.dei.perla.core.channel.simulator.SimulatorMessageDescriptor;
import org.dei.perla.core.descriptor.AttributeDescriptor.AttributeAccessType;
import org.dei.perla.core.descriptor.AttributeDescriptor.AttributePermission;
import org.junit.Test;

import java.io.FileInputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.text.IsEmptyString.isEmptyOrNullString;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class JaxbDeviceDescriptorParserTest {

	private static final String descriptorPath = "src/test/java/org/dei/perla/core/descriptor/device_descriptor.xml";
	private static final Set<String> packages;
	static {
		Set<String> pkgs = new HashSet<>();
		pkgs.add("org.dei.perla.core.descriptor");
		pkgs.add("org.dei.perla.core.descriptor.instructions");
		pkgs.add("org.dei.perla.core.channel.simulator");
		packages = Collections.unmodifiableSet(pkgs);
	}

	@Test
	public void parserCreation() throws Exception {
		DeviceDescriptorParser parser = new JaxbDeviceDescriptorParser(
				packages);

		assertThat(parser, notNullValue());
	}

	@Test
	public void parseXml() throws Exception {
		DeviceDescriptorParser parser;
		DeviceDescriptor descriptor;

		parser = new JaxbDeviceDescriptorParser(packages);
		descriptor = parser.parse(new FileInputStream(descriptorPath));

		assertThat(descriptor, notNullValue());
		assertThat(descriptor.getAttributeList().size(), equalTo(3));
		assertThat(descriptor.getMessageList().size(), equalTo(3));
	}

	@Test
	public void checkAttributes() throws Exception {
		DeviceDescriptorParser parser;
		DeviceDescriptor descriptor;

		parser = new JaxbDeviceDescriptorParser(packages);
		descriptor = parser.parse(new FileInputStream(descriptorPath));

		for (AttributeDescriptor attribute : descriptor.getAttributeList()) {
			assertThat(attribute, notNullValue());
			assertThat(attribute.getId(), not(isEmptyOrNullString()));
			switch (attribute.getId()) {
			case "temperature":
				assertThat(attribute.getType(), equalTo(DataType.FLOAT));
				assertThat(attribute.getAccess(),
						equalTo(AttributeAccessType.DYNAMIC));
				assertThat(attribute.getPermission(),
						equalTo(AttributePermission.READ_WRITE));
				break;
			case "pressure":
				assertThat(attribute.getType(), equalTo(DataType.FLOAT));
				assertThat(attribute.getAccess(),
						equalTo(AttributeAccessType.DYNAMIC));
				assertThat(attribute.getPermission(),
						equalTo(AttributePermission.READ_ONLY));
				break;
			case "room_number":
				assertThat(attribute.getType(), equalTo(DataType.INTEGER));
				assertThat(attribute.getAccess(),
						equalTo(AttributeAccessType.STATIC));
				assertThat(attribute.getPermission(),
						equalTo(AttributePermission.READ_ONLY));
				break;
			default:
				throw new RuntimeException();
			}
		}
	}

	@Test
	public void checkMessageClass() throws Exception {
		DeviceDescriptorParser parser;
		DeviceDescriptor descriptor;

		parser = new JaxbDeviceDescriptorParser(packages);
		descriptor = parser.parse(new FileInputStream(descriptorPath));

		for (MessageDescriptor message : descriptor.getMessageList()) {
			assertThat(message, notNullValue());
			assertThat(message.getId(), not(isEmptyOrNullString()));
			switch (message.getId()) {
			case "temp-only":
				assertTrue(message instanceof SimulatorMessageDescriptor);
				break;
			case "press-only":
				assertTrue(message instanceof SimulatorMessageDescriptor);
				break;
			case "all":
				assertTrue(message instanceof SimulatorMessageDescriptor);
				break;
			default:
				throw new RuntimeException();
			}
		}
	}

	@Test
	public void checkChannelClass() throws Exception {
		DeviceDescriptorParser parser;
		DeviceDescriptor descriptor;

		parser = new JaxbDeviceDescriptorParser(packages);
		descriptor = parser.parse(new FileInputStream(descriptorPath));

		for (ChannelDescriptor channel : descriptor.getChannelList()) {
			assertThat(channel, notNullValue());
			assertThat(channel.getId(), not(isEmptyOrNullString()));
			assertTrue(channel instanceof SimulatorChannelDescriptor);
		}
	}

	@Test
	public void checkRequest() throws Exception {
		DeviceDescriptorParser parser;
		DeviceDescriptor descriptor;

		parser = new JaxbDeviceDescriptorParser(packages);
		descriptor = parser.parse(new FileInputStream(descriptorPath));

		assertThat(descriptor.getRequestList(), notNullValue());
		assertThat(descriptor.getRequestList().size(), equalTo(3));
		for (IORequestDescriptor request : descriptor.getRequestList()) {
			assertTrue(request instanceof SimulatorIORequestDescriptor);
		}
	}

	@Test
	public void checkOperations() throws Exception {
		DeviceDescriptorParser parser;
		DeviceDescriptor descriptor;

		parser = new JaxbDeviceDescriptorParser(packages);
		descriptor = parser.parse(new FileInputStream(descriptorPath));

		assertThat(descriptor.getOperationList(), notNullValue());
		assertThat(descriptor.getOperationList().size(), equalTo(4));
		for (OperationDescriptor operation : descriptor.getOperationList()) {
			switch (operation.getId()) {

			case "periodic":
				assertTrue(operation instanceof PeriodicOperationDescriptor);
				PeriodicOperationDescriptor samp = (PeriodicOperationDescriptor) operation;
				assertThat(samp.getStartScript(), notNullValue());
				assertThat(samp.getStartScript().size(), equalTo(2));
				assertThat(samp.getStopScript(), notNullValue());
				assertThat(samp.getStopScript().size(), equalTo(2));
				assertThat(samp.getOnReceiveList().size(), equalTo(2));

				OnReceiveDescriptor on = samp.getOnReceiveList().get(0);
				assertThat(on.getMessage(), equalTo("temp-only"));
				assertThat(on.getInstructionList().size(), equalTo(2));
				on = samp.getOnReceiveList().get(1);
				assertThat(on.getMessage(), equalTo("press-only"));
				assertThat(on.getInstructionList().size(), equalTo(2));
				break;

			case "get":
				assertTrue(operation instanceof GetOperationDescriptor);
				GetOperationDescriptor get = (GetOperationDescriptor) operation;
				assertThat(get.getScript(), notNullValue());
				assertThat(get.getScript().size(), equalTo(2));
				break;

			case "set":
				assertTrue(operation instanceof SetOperationDescriptor);
				SetOperationDescriptor set = (SetOperationDescriptor) operation;
				assertThat(set.getInstructionList(), notNullValue());
				assertThat(set.getInstructionList().size(), equalTo(2));
				break;

			case "async":
				assertTrue(operation instanceof AsyncOperationDescriptor);
				AsyncOperationDescriptor async = (AsyncOperationDescriptor) operation;
				assertThat(async.getStartScript(), notNullValue());
				assertThat(async.getOnReceive(), notNullValue());
				break;

			default:
				throw new RuntimeException();
			}
		}
	}

}
