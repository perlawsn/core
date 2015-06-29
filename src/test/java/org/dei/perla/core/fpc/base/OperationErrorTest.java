package org.dei.perla.core.fpc.base;

import org.dei.perla.core.channel.Channel;
import org.dei.perla.core.channel.ChannelFactory;
import org.dei.perla.core.channel.IORequestBuilder;
import org.dei.perla.core.channel.IORequestBuilderFactory;
import org.dei.perla.core.channel.loopback.LoopbackChannel;
import org.dei.perla.core.channel.loopback.LoopbackIORequestBuilder;
import org.dei.perla.core.channel.loopback.TestMapper;
import org.dei.perla.core.channel.simulator.SimulatorChannelFactory;
import org.dei.perla.core.channel.simulator.SimulatorIORequestBuilderFactory;
import org.dei.perla.core.channel.simulator.SimulatorMapperFactory;
import org.dei.perla.core.descriptor.*;
import org.dei.perla.core.engine.*;
import org.dei.perla.core.engine.SubmitInstruction.RequestParameter;
import org.dei.perla.core.message.Mapper;
import org.dei.perla.core.message.MapperFactory;
import org.dei.perla.core.sample.SamplePipeline;
import org.dei.perla.core.sample.SamplePipeline.PipelineBuilder;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * @author Guido Rota 23/05/15.
 */
public class OperationErrorTest {

    private static final String descriptorPath =
            "src/test/java/org/dei/perla/core/fpc/base/fpc_descriptor.xml";

    private static OneoffOperation getOp;

    @BeforeClass
    public static void setup() throws Exception {
        JAXBContext jc = JAXBContext.newInstance("org.dei.perla.core.descriptor"
                + ":org.dei.perla.core.descriptor.instructions"
                + ":org.dei.perla.core.channel.simulator");

        Unmarshaller unmarshaller = jc.createUnmarshaller();
        StreamSource xml = new StreamSource(descriptorPath);
        DeviceDescriptor device = unmarshaller.unmarshal(xml,
                DeviceDescriptor.class).getValue();

        // Attributes
        Map<String, AttributeDescriptor> attributeMap = new HashMap<>();
        for (AttributeDescriptor attDesc : device.getAttributeList()) {
            attributeMap.put(attDesc.getId(), attDesc);
        }

        // Message mappers
        Map<String, Mapper> mmMap = new HashMap<>();
        mmMap.put("message1", new TestMapper("message1"));
        for (MessageDescriptor msgDesc : device.getMessageList()) {
            MapperFactory fct = new SimulatorMapperFactory();
            mmMap.put(msgDesc.getId(), fct.createMapper(msgDesc, null, null));
        }

        // Channels
        Map<String, Channel> chMap = new HashMap<>();
        chMap.put("loopback-channel", new LoopbackChannel());
        for (ChannelDescriptor chDesc : device.getChannelList()) {
            ChannelFactory fct = new SimulatorChannelFactory();
            chMap.put(chDesc.getId(), fct.createChannel(chDesc));
        }

        // RequestBuilders
        Map<String, IORequestBuilder> builMap = new HashMap<>();
        builMap.put("request1", new LoopbackIORequestBuilder("request1"));
        for (IORequestDescriptor reqDesc : device.getRequestList()) {
            IORequestBuilderFactory fct = new SimulatorIORequestBuilderFactory();
            builMap.put(reqDesc.getId(), fct.create(reqDesc));
        }

        // Get operation and simulated periodic operation
        RequestParameter getParameterArray[] = new RequestParameter[] { new RequestParameter(
                "test", "test", mmMap.get("message1")) };

        Script getScript = ScriptBuilder
                .newScript()
                .add(new CreateComplexVarInstruction("test",
                        mmMap.get("message1")))
                .add(new SetComplexInstruction("test", "integer", Integer.class, "5"))
                .add(new SetComplexInstruction("test", "float", Float.class, "5.2"))
                .add(new SetComplexInstruction("test", "boolean", Boolean.class,
                        "false"))
                .add(new SetComplexInstruction("test", "string", String.class, "test"))
                .add(new SubmitInstruction(builMap.get("request1"), chMap
                        .get("loopback-channel"), getParameterArray, "res",
                        mmMap.get("message1")))
                .add(new PutInstruction("${res.integer}",
                        attributeMap.get("integer"), 0))
                .add(new PutInstruction("${res.float}",
                        attributeMap.get("float"), 1))
                .add(new PutInstruction("${res.boolean}",
                        attributeMap.get("boolean"), 2))
                .add(new PutInstruction("${res.string}",
                        attributeMap.get("string"), 3))
                .add(new PutInstruction("${now()}",
                        attributeMap.get("timestamp"), 4))
                .add(new EmitInstruction()).add(new StopInstruction())
                .buildScript("test");
        getOp = new OneoffOperation("test", getScript.getEmit(), getScript);
    }

    @Test
    public void getError() throws InterruptedException {
        PipelineBuilder pb = SamplePipeline.newBuilder(getOp.getAttributes());
        ErrorTaskHandler handler = new ErrorTaskHandler();
        BaseTask t = getOp.doSchedule(null, handler, pb.create());
        t.start();
        Throwable err = handler.awaitError();
        assertThat(err, notNullValue());
    }


}
