package org.dei.perla.core.fpc.base;

import org.dei.perla.core.channel.ChannelFactory;
import org.dei.perla.core.channel.IORequestBuilderFactory;
import org.dei.perla.core.channel.simulator.SimulatorChannelFactory;
import org.dei.perla.core.channel.simulator.SimulatorIORequestBuilderFactory;
import org.dei.perla.core.channel.simulator.SimulatorMapperFactory;
import org.dei.perla.core.descriptor.DeviceDescriptor;
import org.dei.perla.core.descriptor.DeviceDescriptorParser;
import org.dei.perla.core.descriptor.JaxbDeviceDescriptorParser;
import org.dei.perla.core.engine.*;
import org.dei.perla.core.fpc.DataType;
import org.dei.perla.core.fpc.Fpc;
import org.dei.perla.core.fpc.FpcFactory;
import org.dei.perla.core.message.MapperFactory;
import org.dei.perla.core.fpc.Attribute;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.FileInputStream;
import java.util.*;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.*;

public class BaseFpcFactoryTest {

    private static final Attribute intAtt =
            Attribute.create("integer", DataType.INTEGER);
    private static final Attribute floatAtt =
            Attribute.create("float", DataType.FLOAT);
    private static final Attribute stringAtt =
            Attribute.create("string", DataType.STRING);
    private static final Attribute boolAtt =
            Attribute.create("boolean", DataType.BOOLEAN);

    private static final String descriptorPath =
            "src/test/java/org/dei/perla/core/fpc/base/fpc_descriptor.xml";
    private static final Set<String> packages;
    static {
        Set<String> pkgs = new HashSet<>();
        pkgs.add("org.dei.perla.core.descriptor");
        pkgs.add("org.dei.perla.core.descriptor.instructions");
        pkgs.add("org.dei.perla.core.channel.simulator");
        packages = Collections.unmodifiableSet(pkgs);
    }
    private static BaseFpc baseFpc;
    private static Scheduler scheduler;

    @BeforeClass
    public static void setup() throws Exception {
        DeviceDescriptorParser parser = new JaxbDeviceDescriptorParser(
                packages);
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
        Fpc fpc = factory.createFpc(descriptor, 1);
        assertThat(fpc, notNullValue());
        assertThat(fpc.getId(), equalTo(1));

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
        assertTrue(attributes.contains(intAtt));
        assertFalse(attributes.contains(floatAtt));
        assertFalse(attributes.contains(boolAtt));
        assertFalse(attributes.contains(stringAtt));

        op = scheduler.getGetOperation("string-get");
        assertThat(op, notNullValue());
        assertTrue(op instanceof OneoffOperation);
        attributes = op.getAttributes();
        assertTrue(attributes.contains(stringAtt));
        assertFalse(attributes.contains(floatAtt));
        assertFalse(attributes.contains(boolAtt));
        assertFalse(attributes.contains(intAtt));
    }

    @Test
    public void checkSetOperation() {
        Operation op;
        Collection<Attribute> attributes;

        op = scheduler.getSetOperation("integer-set");
        assertThat(op, notNullValue());
        assertTrue(op instanceof OneoffOperation);
        attributes = op.getAttributes();
        assertTrue(attributes.contains(intAtt));
        assertFalse(attributes.contains(floatAtt));
        assertFalse(attributes.contains(boolAtt));
        assertFalse(attributes.contains(stringAtt));
    }

    @Test
    public void checkSimulatedPeriodicOperations() {
        Operation op;
        Collection<Attribute> attributes;

        op = scheduler.getPeriodicOperation("_integer-get_sim");
        assertThat(op, notNullValue());
        attributes = op.getAttributes();
        assertTrue(op instanceof SimulatedPeriodicOperation);
        assertTrue(attributes.contains(intAtt));
        assertFalse(attributes.contains(floatAtt));
        assertFalse(attributes.contains(boolAtt));
        assertFalse(attributes.contains(stringAtt));

        op = scheduler.getPeriodicOperation("_string-get_sim");
        assertThat(op, notNullValue());
        attributes = op.getAttributes();
        assertTrue(op instanceof SimulatedPeriodicOperation);
        assertTrue(attributes.contains(stringAtt));
        assertFalse(attributes.contains(floatAtt));
        assertFalse(attributes.contains(boolAtt));
        assertFalse(attributes.contains(intAtt));
    }

    @Test
    public void checkNativePeriodicOperations() {
        Operation op;
        Collection<Attribute> attributes;

        op = scheduler.getPeriodicOperation("all-periodic");
        assertThat(op, notNullValue());
        assertTrue(op instanceof NativePeriodicOperation);
        attributes = op.getAttributes();
        assertTrue(attributes.contains(stringAtt));
        assertTrue(attributes.contains(floatAtt));
        assertTrue(attributes.contains(intAtt));
        assertFalse(attributes.contains(boolAtt));
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
        assertTrue(i instanceof CreateComplexVarInstruction);
        i = i.next();
        assertTrue(i instanceof SetComplexInstruction);
        i = i.next();
        assertTrue(i instanceof SubmitInstruction);
        i = i.next();
        assertTrue(i instanceof StopInstruction);
    }

}
