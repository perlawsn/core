package org.dei.perla.fpc.base;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.dei.perla.channel.ChannelFactory;
import org.dei.perla.channel.IORequestBuilderFactory;
import org.dei.perla.channel.simulator.SimulatorChannelFactory;
import org.dei.perla.channel.simulator.SimulatorIORequestBuilderFactory;
import org.dei.perla.channel.simulator.SimulatorMapperFactory;
import org.dei.perla.fpc.Attribute;
import org.dei.perla.fpc.Fpc;
import org.dei.perla.fpc.FpcFactory;
import org.dei.perla.fpc.descriptor.DataType;
import org.dei.perla.fpc.descriptor.DeviceDescriptor;
import org.dei.perla.fpc.descriptor.DeviceDescriptorParser;
import org.dei.perla.fpc.descriptor.JaxbDeviceDescriptorParser;
import org.dei.perla.fpc.engine.CreateComplexInstruction;
import org.dei.perla.fpc.engine.EmitInstruction;
import org.dei.perla.fpc.engine.Instruction;
import org.dei.perla.fpc.engine.PutInstruction;
import org.dei.perla.fpc.engine.Script;
import org.dei.perla.fpc.engine.SetComplexInstruction;
import org.dei.perla.fpc.engine.StopInstruction;
import org.dei.perla.fpc.engine.SubmitInstruction;
import org.dei.perla.message.MapperFactory;
import org.junit.BeforeClass;
import org.junit.Test;

public class BaseFpcFactoryTest {

	private static final Attribute intAttribute = new Attribute("integer", DataType.INTEGER);
	private static final Attribute floatAttribute = new Attribute("float", DataType.FLOAT);
	private static final Attribute stringAttribute = new Attribute("string", DataType.STRING);
	private static final Attribute booleanAttribute = new Attribute("boolean", DataType.BOOLEAN);

	private static final String descriptorPath = "src/test/java/org/dei/perla/fpc/base/fpc_descriptor.xml";
	private static List<String> packageList = Arrays.asList(new String[] {
			"org.dei.perla.fpc.descriptor",
			"org.dei.perla.fpc.descriptor.instructions",
			"org.dei.perla.channel.simulator" });
	private static BaseFpc baseFpc;
	private static OperationScheduler scheduler;

	@BeforeClass
	public static void setup() throws Exception {
		DeviceDescriptorParser parser = new JaxbDeviceDescriptorParser(
				packageList);
		DeviceDescriptor descriptor = parser.parse(new FileInputStream(
				descriptorPath));

		List<MapperFactory> mhfList = new ArrayList<>();
		mhfList.add(new SimulatorMapperFactory());
		List<ChannelFactory> chfList = new ArrayList<>();
		chfList.add(new SimulatorChannelFactory());
		List<IORequestBuilderFactory> rbfList = new ArrayList<>();
		rbfList.add(new SimulatorIORequestBuilderFactory());
		FpcFactory factory = new BaseFpcFactory(mhfList, chfList, rbfList);

		assertThat(descriptor, notNullValue());
		Fpc fpc = factory.createFpc(descriptor, 0);
		assertThat(fpc, notNullValue());
		assertThat(fpc.getId(), equalTo(0));

		assertTrue(fpc instanceof BaseFpc);
		baseFpc = (BaseFpc) fpc;

		scheduler = baseFpc.getOperationScheduler();
		assertThat(scheduler, notNullValue());
	}

	@Test
	public void checkAttributes() {
		Attribute att;
		Collection<Attribute> attributes = baseFpc.getAttributes();

		att = find("integer", attributes);
		assertThat(att, notNullValue());
		assertThat(att.getId(), equalTo("integer"));
		assertThat(att.getType(), equalTo(DataType.INTEGER));

		att = find("float", attributes);
		assertThat(att, notNullValue());
		assertThat(att.getId(), equalTo("float"));
		assertThat(att.getType(), equalTo(DataType.FLOAT));

		att = find("boolean", attributes);
		assertThat(att, notNullValue());
		assertThat(att.getId(), equalTo("boolean"));
		assertThat(att.getType(), equalTo(DataType.BOOLEAN));

		att = find("string", attributes);
		assertThat(att, notNullValue());
		assertThat(att.getId(), equalTo("string"));
		assertThat(att.getType(), equalTo(DataType.STRING));

		att = find("period", attributes);
		assertThat(att, notNullValue());
		assertThat(att.getId(), equalTo("period"));
		assertThat(att.getType(), equalTo(DataType.INTEGER));

		att = find("static", attributes);
		assertThat(att, notNullValue());
		assertThat(att.getId(), equalTo("static"));
		assertThat(att.getType(), equalTo(DataType.INTEGER));
		assertTrue(att instanceof StaticAttribute);
		StaticAttribute staticAtt = (StaticAttribute) att;
		assertThat(staticAtt.getValue(), notNullValue());
		assertTrue(staticAtt.getValue() instanceof Integer);
		Integer value = (Integer) staticAtt.getValue();
		assertThat(value, equalTo(5));
	}

	private static Attribute find(String id, Collection<Attribute> attributes) {
		for (Attribute a : attributes) {
			if (a.getId().equals(id)) {
				return a;
			}
		}
		return null;
	}

    @Test
    public void checkType() {
        assertThat(baseFpc.getType(), equalTo("test"));
    }

	@Test
	public void checkGetOperations() {
		Operation op;
		Collection<Attribute> attributes;

		op = scheduler.getGetOperation("integer-get");
		assertThat(op, notNullValue());
		assertTrue(op instanceof OneoffOperation);
		attributes = op.getAttributes();
		assertTrue(attributes.contains(intAttribute));
		assertFalse(attributes.contains(floatAttribute));
		assertFalse(attributes.contains(booleanAttribute));
		assertFalse(attributes.contains(stringAttribute));

		op = scheduler.getGetOperation("string-get");
		assertThat(op, notNullValue());
		assertTrue(op instanceof OneoffOperation);
		attributes = op.getAttributes();
		assertTrue(attributes.contains(stringAttribute));
		assertFalse(attributes.contains(floatAttribute));
		assertFalse(attributes.contains(booleanAttribute));
		assertFalse(attributes.contains(intAttribute));
	}

	@Test
	public void checkSetOperation() {
		Operation op;
		Collection<Attribute> attributes;

		op = scheduler.getSetOperation("integer-set");
		assertThat(op, notNullValue());
		assertTrue(op instanceof OneoffOperation);
		attributes = op.getAttributes();
		assertTrue(attributes.contains(intAttribute));
		assertFalse(attributes.contains(floatAttribute));
		assertFalse(attributes.contains(booleanAttribute));
		assertFalse(attributes.contains(stringAttribute));
	}

	@Test
	public void checkSimulatedPeriodicOperations() {
		Operation op;
		Collection<Attribute> attributes;

		op = scheduler.getPeriodicOperation("_integer-get_sim");
		assertThat(op, notNullValue());
		attributes = op.getAttributes();
		assertTrue(op instanceof SimulatedPeriodicOperation);
		assertTrue(attributes.contains(intAttribute));
		assertFalse(attributes.contains(floatAttribute));
		assertFalse(attributes.contains(booleanAttribute));
		assertFalse(attributes.contains(stringAttribute));

		op = scheduler.getPeriodicOperation("_string-get_sim");
		assertThat(op, notNullValue());
		attributes = op.getAttributes();
		assertTrue(op instanceof SimulatedPeriodicOperation);
		assertTrue(attributes.contains(stringAttribute));
		assertFalse(attributes.contains(floatAttribute));
		assertFalse(attributes.contains(booleanAttribute));
		assertFalse(attributes.contains(intAttribute));
	}

	@Test
	public void checkNativePeriodicOperations() {
		Operation op;
		Collection<Attribute> attributes;

		op = scheduler.getPeriodicOperation("all-periodic");
		assertThat(op, notNullValue());
		assertTrue(op instanceof NativePeriodicOperation);
		attributes = op.getAttributes();
		assertTrue(attributes.contains(stringAttribute));
		assertTrue(attributes.contains(floatAttribute));
		assertTrue(attributes.contains(intAttribute));
		assertFalse(attributes.contains(booleanAttribute));
	}

	@Test
	public void checkScriptParsing() {
		Operation op;
		Script script;
		Instruction i;

		// integer-get script
		op = scheduler.getGetOperation("integer-get");
		assertTrue(op instanceof OneoffOperation);
		script = ((OneoffOperation) op).getScript();

		i = script.getCode();
		assertTrue(i instanceof SubmitInstruction);
		i = i.next();
		assertTrue(i instanceof PutInstruction);
		i = i.next();
		assertTrue(i instanceof EmitInstruction);
		i = i.next();
		assertTrue(i instanceof StopInstruction);

		// all-periodic start script
		op = scheduler.getPeriodicOperation("all-periodic");
		assertTrue(op instanceof NativePeriodicOperation);
		script = ((NativePeriodicOperation) op).getStartScript();

		i = script.getCode();
		assertTrue(i instanceof CreateComplexInstruction);
		i = i.next();
		assertTrue(i instanceof SetComplexInstruction);
		i = i.next();
		assertTrue(i instanceof SubmitInstruction);
		i = i.next();
		assertTrue(i instanceof StopInstruction);
	}

}
