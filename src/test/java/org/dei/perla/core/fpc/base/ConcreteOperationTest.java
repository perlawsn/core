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
import org.dei.perla.core.descriptor.ChannelDescriptor;
import org.dei.perla.core.descriptor.DeviceDescriptor;
import org.dei.perla.core.descriptor.IORequestDescriptor;
import org.dei.perla.core.descriptor.MessageDescriptor;
import org.dei.perla.core.engine.*;
import org.dei.perla.core.engine.SubmitInstruction.RequestParameter;
import org.dei.perla.core.fpc.Attribute;
import org.dei.perla.core.fpc.DataType;
import org.dei.perla.core.fpc.Sample;
import org.dei.perla.core.fpc.base.AsyncOperation.AsyncMessageHandler;
import org.dei.perla.core.message.Mapper;
import org.dei.perla.core.message.MapperFactory;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import java.time.Instant;
import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class ConcreteOperationTest {

    private static final Attribute intAtt =
            Attribute.create("integer", DataType.INTEGER);
    private static final Attribute floatAtt =
            Attribute.create("float", DataType.FLOAT);
    private static final Attribute stringAtt =
            Attribute.create("string", DataType.STRING);
    private static final Attribute boolAtt =
            Attribute.create("boolean", DataType.BOOLEAN);
    private static final Attribute tsAtt =
            Attribute.create("timestamp", DataType.TIMESTAMP);
    private static final Attribute eventAtt =
            Attribute.create("event", DataType.INTEGER);

    private static final String descriptorPath =
            "src/test/java/org/dei/perla/core/fpc/base/fpc_descriptor.xml";

    private static ChannelManager chMgr;
    private static OneoffOperation setOp;
    private static OneoffOperation getOp;
    private static NativePeriodicOperation natPeriodicOp;
    private static SimulatedPeriodicOperation simPeriodicOp;
    private static AsyncOperation asyncOp;

    @BeforeClass
    public static void setup() throws Exception {
        JAXBContext jc = JAXBContext.newInstance("org.dei.perla.core.descriptor"
                + ":org.dei.perla.core.descriptor.instructions"
                + ":org.dei.perla.core.channel.simulator");

        Unmarshaller unmarshaller = jc.createUnmarshaller();
        StreamSource xml = new StreamSource(descriptorPath);
        DeviceDescriptor device = unmarshaller.unmarshal(xml,
                DeviceDescriptor.class).getValue();

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

        // Create ChannelManager
        chMgr = new ChannelManager(new ArrayList<>(chMap.values()));

        // Native periodic operation
        RequestParameter[] paramArray = new RequestParameter[1];
        paramArray[0] = new RequestParameter("period", "period-message",
                mmMap.get("sampling-period"));

        Script perStartScript = ScriptBuilder
                .newScript()
                .add(new CreateComplexVarInstruction("period-message", mmMap
                        .get("sampling-period")))
                .add(new SetComplexInstruction("period-message", "period",
                        Integer.class, "${param['period']}"))
                .add(new SubmitInstruction(builMap.get("all-request"), chMap
                        .get("simulator"), paramArray, null, null))
                .buildScript("start_script");

        Script perStopScript = ScriptBuilder
                .newScript()
                .add(new CreateComplexVarInstruction("period-message", mmMap
                        .get("sampling-period")))
                .add(new SetComplexInstruction("period-message", "period",
                        Integer.class, "${param['period']}"))
                .add(new SubmitInstruction(builMap.get("all-request"), chMap
                        .get("simulator"), paramArray, null, null))
                .buildScript("stop_script");

        Script perOnScript = ScriptBuilder.newScript()
                .add(new PutInstruction("${result.integer}",
                        Integer.class, 0), intAtt)
                .add(new PutInstruction("${result.float}",
                        Float.class, 1), floatAtt)
                .add(new PutInstruction("${result.string}",
                        String.class, 2), stringAtt)
                .add(new EmitInstruction()).buildScript("on_script");
        List<MessageScript> perHandlerList = new ArrayList<>();
        perHandlerList.add(new MessageScript(perOnScript, mmMap.get("all-msg"),
                true, "result", 0));

        natPeriodicOp = new NativePeriodicOperation("periodic_operation",
                perOnScript.getEmit(), perStartScript, perStopScript,
                perHandlerList, chMgr);

        // Asynchronous operation
        RequestParameter[] asyncParamArray = new RequestParameter[1];
        asyncParamArray[0] = new RequestParameter("period", "period-message",
                mmMap.get("sampling-period"));

        Script asyncStartScript = ScriptBuilder
                .newScript()
                .add(new CreateComplexVarInstruction("period-message", mmMap
                        .get("sampling-period")))
                .add(new SetComplexInstruction("period-message", "period",
                        Integer.class, "50"))
                .add(new SubmitInstruction(builMap.get("event-request"), chMap
                        .get("simulator"), asyncParamArray, null, null))
                .buildScript("start_script");

        Script asyncOnScript = ScriptBuilder.newScript()
                .add(new PutInstruction("${result.event}",
                        Integer.class, 0), eventAtt)
                .add(new EmitInstruction()).buildScript("on_script");
        AsyncMessageHandler handler = new AsyncMessageHandler(
                mmMap.get("event-msg"), asyncOnScript, "result");

        asyncOp = new AsyncOperation("async_operation", asyncOnScript.getEmit(),
                asyncStartScript, handler, chMgr);
        asyncOp.start();

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
                        Integer.class, 0), intAtt)
                .add(new PutInstruction("${res.float}",
                        Float.class, 1), floatAtt)
                .add(new PutInstruction("${res.boolean}",
                        Boolean.class, 2), boolAtt)
                .add(new PutInstruction("${res.string}",
                        String.class, 3), stringAtt)
                .add(new PutInstruction("${now()}",
                        Instant.class, 4), tsAtt)
                .add(new EmitInstruction()).add(new StopInstruction())
                .buildScript("test");
        getOp = new OneoffOperation("test", getScript.getEmit(), getScript);
        simPeriodicOp = new SimulatedPeriodicOperation("test_sim", getScript);

        // Set operation
        RequestParameter setParameterArray[] = new RequestParameter[] { new RequestParameter(
                "test", "test", mmMap.get("message1")) };

        Script script = ScriptBuilder
                .newScript()
                .add(new CreateComplexVarInstruction("test",
                        mmMap.get("message1")))
                .add(new SetComplexInstruction("test", "integer", Integer.class, "5"))
                .add(new SetComplexInstruction("test", "float", Float.class, "5.2"))
                .add(new SetComplexInstruction("test", "boolean", Boolean.class,
                        "false"))
                .add(new SetComplexInstruction("test", "string", String.class, "test"))
                .add(new SubmitInstruction(builMap.get("request1"), chMap
                        .get("loopback-channel"), setParameterArray, "res",
                        mmMap.get("message1"))).add(new StopInstruction())
                .buildScript("test");

        setOp = new OneoffOperation("test", script.getEmit(), script);
    }

    @Test
    public void testGetOperation() throws Exception {
        SamplePipeline pipe = new SamplePipeline(getOp.getAttributes());
        LatchingTaskHandler syncHandler = new LatchingTaskHandler(1);
        BaseTask task = getOp.schedule(null, syncHandler, pipe);
        assertThat(task, notNullValue());
        assertFalse(task.isRunning());
        task.start();
        assertTrue(getOp.getAttributes().containsAll(task.getAttributes()));
        assertTrue(task.getAttributes().containsAll(getOp.getAttributes()));

        Sample sample = syncHandler.getLastSample();
        assertFalse(task.isRunning());
        assertThat(sample, notNullValue());

        Object value = sample.getValue("integer");
        assertThat(value, notNullValue());
        assertTrue(value instanceof Integer);
        assertThat((Integer) value, equalTo(5));

        value = sample.getValue("float");
        assertThat(value, notNullValue());
        assertTrue(value instanceof Float);
        assertThat((Float) value, equalTo(5.2f));

        value = sample.getValue("boolean");
        assertThat(value, notNullValue());
        assertTrue(value instanceof Boolean);
        assertThat((Boolean) value, equalTo(false));

        value = sample.getValue("string");
        assertThat(value, notNullValue());
        assertTrue(value instanceof String);
        assertThat((String) value, equalTo("test"));

        assertThat(getOp.taskCount(), equalTo(0));
    }

    @Test
    public void testSetOperation() throws Exception {
        SamplePipeline pipe = new SamplePipeline(getOp.getAttributes());
        LatchingTaskHandler handler = new LatchingTaskHandler(1);
        BaseTask task = setOp.schedule(null, handler, pipe);
        assertThat(task, notNullValue());
        assertFalse(task.isRunning());
        task.start();

        handler.awaitCompletion();
        assertFalse(task.isRunning());
        assertThat(getOp.taskCount(), equalTo(0));
    }

    @Test
    public void singleNativePeriodicOperation() throws InterruptedException {
        LatchingTaskHandler handler = new LatchingTaskHandler(1000);
        SamplePipeline pipe = new SamplePipeline(natPeriodicOp.getAttributes());
        Map<String, Object> parameterMap = new HashMap<>();
        parameterMap.put("period", 1);

        // Start test
        BaseTask task = natPeriodicOp.schedule(parameterMap, handler, pipe);
        assertThat(task, notNullValue());
        assertFalse(task.isRunning());
        task.start();
        assertTrue(task.isRunning());
        assertTrue(natPeriodicOp.getAttributes().containsAll(
                task.getAttributes()));
        assertTrue(task.getAttributes().containsAll(
                natPeriodicOp.getAttributes()));
        assertThat(handler.getAveragePeriod(), greaterThanOrEqualTo(0.8d));
        assertThat(handler.getAveragePeriod(), lessThanOrEqualTo(1.2d));

        // Stop test
        task.stop();
        int initialCount = handler.getCount();
        Thread.sleep(1000);
        assertThat(handler.getCount(), equalTo(initialCount));
    }

    @Test
    public void multipleNativePeriodicOperations() throws Exception {
        LatchingTaskHandler h1 = new LatchingTaskHandler(10);
        LatchingTaskHandler h2 = new LatchingTaskHandler(100);
        LatchingTaskHandler h3 = new LatchingTaskHandler(1000);

        Map<String, Object> paramMap1 = new HashMap<>();
        paramMap1.put("period", 100);
        Map<String, Object> paramMap2 = new HashMap<>();
        paramMap2.put("period", 10);
        Map<String, Object> paramMap3 = new HashMap<>();
        paramMap3.put("period", 1);

        SamplePipeline pipe = new SamplePipeline(natPeriodicOp.getAttributes());

        BaseTask task1 = natPeriodicOp.schedule(paramMap1, h1, pipe);
        assertThat(task1, notNullValue());
        assertFalse(task1.isRunning());
        task1.start();
        assertTrue(task1.isRunning());
        assertThat(natPeriodicOp.getSamplingPeriod(), equalTo(100l));
        BaseTask task2 = natPeriodicOp.schedule(paramMap2, h2, pipe);
        assertThat(task2, notNullValue());
        assertFalse(task2.isRunning());
        task2.start();
        assertTrue(task2.isRunning());
        assertThat(natPeriodicOp.getSamplingPeriod(), equalTo(10l));
        BaseTask task3 = natPeriodicOp.schedule(paramMap3, h3, pipe);
        assertThat(task3, notNullValue());
        assertFalse(task3.isRunning());
        task3.start();
        assertTrue(task3.isRunning());
        assertThat(natPeriodicOp.getSamplingPeriod(), equalTo(1l));

        assertThat(h1.getCount(), greaterThanOrEqualTo(10));
        assertThat(h1.getCount(), lessThanOrEqualTo(12));
        assertThat(h1.getAveragePeriod(), greaterThanOrEqualTo(80d));
        assertThat(h1.getAveragePeriod(), lessThanOrEqualTo(120d));

        assertThat(h2.getCount(), greaterThanOrEqualTo(100));
        assertThat(h2.getCount(), lessThanOrEqualTo(110));
        assertThat(h2.getAveragePeriod(), greaterThanOrEqualTo(8d));
        assertThat(h2.getAveragePeriod(), lessThanOrEqualTo(12d));

        assertThat(h3.getCount(), greaterThanOrEqualTo(1000));
        assertThat(h3.getCount(), lessThanOrEqualTo(1010));
        assertThat(h3.getAveragePeriod(), greaterThanOrEqualTo(0.8d));
        assertThat(h3.getAveragePeriod(), lessThanOrEqualTo(1.2d));

        int initialH1Count;
        int initialH2Count;
        int initialH3Count;

        task1.stop();
        assertFalse(task1.isRunning());
        assertThat(natPeriodicOp.getSamplingPeriod(), equalTo(1l));
        initialH1Count = h1.getCount();
        initialH2Count = h2.getCount();
        initialH3Count = h3.getCount();
        Thread.sleep(500);
        assertThat(h1.getCount() - initialH1Count, equalTo(0));
        assertThat(h2.getCount() - initialH2Count, greaterThan(0));
        assertThat(h3.getCount() - initialH3Count, greaterThan(0));

        task3.stop();
        assertThat(natPeriodicOp.getSamplingPeriod(), equalTo(10l));
        assertFalse(task3.isRunning());
        initialH1Count = h1.getCount();
        initialH2Count = h2.getCount();
        initialH3Count = h3.getCount();
        Thread.sleep(500);
        assertThat(h1.getCount() - initialH1Count, equalTo(0));
        assertThat(h2.getCount() - initialH2Count, greaterThan(0));
        assertThat(h3.getCount() - initialH3Count, equalTo(0));

        task2.stop();
        assertThat(natPeriodicOp.getSamplingPeriod(), equalTo(0l));
        assertFalse(task2.isRunning());
        initialH1Count = h1.getCount();
        initialH2Count = h2.getCount();
        initialH3Count = h3.getCount();
        Thread.sleep(500);
        assertThat(h1.getCount() - initialH1Count, equalTo(0));
        assertThat(h2.getCount() - initialH2Count, equalTo(0));
        assertThat(h3.getCount() - initialH3Count, equalTo(0));
    }

    @Test
    public void singleSimulatedPeriodicOperation() throws InterruptedException {
        LatchingTaskHandler handler = new LatchingTaskHandler(100);
        Map<String, Object> parameterMap = new HashMap<>();
        parameterMap.put("period", 10);
        SamplePipeline pipe = new SamplePipeline(simPeriodicOp.getAttributes());

        // Start test
        BaseTask task = simPeriodicOp.schedule(parameterMap, handler, pipe);
        assertThat(task, notNullValue());
        assertFalse(task.isRunning());
        task.start();
        assertTrue(task.isRunning());
        assertTrue(task.isRunning());
        assertThat(handler.getAveragePeriod(), greaterThanOrEqualTo(8d));
        assertThat(handler.getAveragePeriod(), lessThanOrEqualTo(12d));

        // Stop test
        task.stop();
        int initialCount = handler.getCount();
        Thread.sleep(1000);
        assertThat(handler.getCount(), equalTo(initialCount));
    }

    @Test
    public void multipleSimulatedPeriodicOperations() throws Exception {
        LatchingTaskHandler h1 = new LatchingTaskHandler(10);
        LatchingTaskHandler h2 = new LatchingTaskHandler(100);
        LatchingTaskHandler h3 = new LatchingTaskHandler(1000);

        Map<String, Object> paramMap1 = new HashMap<>();
        paramMap1.put("period", 100);
        Map<String, Object> paramMap2 = new HashMap<>();
        paramMap2.put("period", 10);
        Map<String, Object> paramMap3 = new HashMap<>();
        paramMap3.put("period", 1);

        SamplePipeline p = new SamplePipeline(
                simPeriodicOp.getAttributes());

        BaseTask task1 = simPeriodicOp.schedule(paramMap1, h1, p);
        assertThat(task1, notNullValue());
        assertFalse(task1.isRunning());
        task1.start();
        assertTrue(task1.isRunning());
        assertThat(simPeriodicOp.getSamplingPeriod(), equalTo(100l));
        BaseTask task2 = simPeriodicOp.schedule(paramMap2, h2, p);
        assertThat(task2, notNullValue());
        assertFalse(task2.isRunning());
        task2.start();
        assertTrue(task2.isRunning());
        assertThat(simPeriodicOp.getSamplingPeriod(), equalTo(10l));
        BaseTask task3 = simPeriodicOp.schedule(paramMap3, h3, p);
        assertThat(task3, notNullValue());
        assertFalse(task3.isRunning());
        task3.start();
        assertTrue(task3.isRunning());
        assertThat(simPeriodicOp.getSamplingPeriod(), equalTo(1l));

        assertThat(h1.getAveragePeriod(), greaterThanOrEqualTo(80d));
        assertThat(h1.getAveragePeriod(), lessThanOrEqualTo(120d));

        assertThat(h2.getAveragePeriod(), greaterThanOrEqualTo(8d));
        assertThat(h2.getAveragePeriod(), lessThanOrEqualTo(12d));

        assertThat(h3.getAveragePeriod(), greaterThanOrEqualTo(0.8d));
        assertThat(h3.getAveragePeriod(), lessThanOrEqualTo(1.2d));

        int initialH1Count;
        int initialH2Count;
        int initialH3Count;

        task1.stop();
        assertFalse(task1.isRunning());
        assertThat(simPeriodicOp.getSamplingPeriod(), equalTo(1l));
        initialH1Count = h1.getCount();
        initialH2Count = h2.getCount();
        initialH3Count = h3.getCount();
        Thread.sleep(500);
        assertThat(h1.getCount() - initialH1Count, equalTo(0));
        assertThat(h2.getCount() - initialH2Count, greaterThan(0));
        assertThat(h3.getCount() - initialH3Count, greaterThan(0));

        task3.stop();
        assertThat(simPeriodicOp.getSamplingPeriod(), equalTo(10l));
        assertFalse(task3.isRunning());
        initialH1Count = h1.getCount();
        initialH2Count = h2.getCount();
        initialH3Count = h3.getCount();
        Thread.sleep(500);
        assertThat(h1.getCount() - initialH1Count, equalTo(0));
        assertThat(h2.getCount() - initialH2Count, greaterThan(0));
        assertThat(h3.getCount() - initialH3Count, equalTo(0));

        task2.stop();
        assertThat(simPeriodicOp.getSamplingPeriod(), equalTo(0l));
        assertFalse(task2.isRunning());
        initialH1Count = h1.getCount();
        initialH2Count = h2.getCount();
        initialH3Count = h3.getCount();
        Thread.sleep(500);
        assertThat(h1.getCount() - initialH1Count, equalTo(0));
        assertThat(h2.getCount() - initialH2Count, equalTo(0));
        assertThat(h3.getCount() - initialH3Count, equalTo(0));
    }

    @Test
    public void asyncSimulatedOneoffOperation() throws Exception {
        LatchingTaskHandler handler = new LatchingTaskHandler(1);

        Operation op = new AsyncOneoffOperation(asyncOp);
        BaseTask task = op.schedule(Collections.emptyMap(), handler);
        assertThat(task, notNullValue());
        assertFalse(task.isRunning());
        task.start();
        Sample sample = handler.getLastSample();
        assertThat(sample, notNullValue());
    }

    @Test
    public void asyncSimulatedPeriodicOperation() throws Exception {
        LatchingTaskHandler handler = new LatchingTaskHandler(3);

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("period", 500);
        PeriodicOperation op = new AsyncPeriodicOperation(asyncOp);
        BaseTask task = op.schedule(paramMap, handler);
        assertThat(task, notNullValue());
        assertFalse(task.isRunning());
        task.start();
        assertTrue(task.isRunning());
        assertThat(op.taskCount(), equalTo(1));

        assertTrue(task instanceof PeriodicTask);
        List<Sample> res = handler.getSamples();
        Object current;
        Object previous = -1;
        for (Sample s : res) {
            assertThat(s, notNullValue());
            current = s.getValue("event");
            assertThat(previous, not(equalTo(current)));
            previous = current;
        }

        task.stop();
        assertFalse(task.isRunning());
        assertThat(op.taskCount(), equalTo(0));
    }

    @Test
    public void singleAsyncRequest() throws Exception {
        LatchingTaskHandler handler = new LatchingTaskHandler(10);

        BaseTask task = asyncOp.schedule(Collections.emptyMap(), handler);
        assertThat(task, notNullValue());
        assertFalse(task.isRunning());
        task.start();
        assertTrue(task.isRunning());

        List<Sample> res = handler.getSamples();
        int current;
        int previous = (int) -1;
        for (Sample s : res) {
            assertThat(s, notNullValue());
            current = (int) s.getValue("event");
            assertThat(previous, not(equalTo(current)));
            previous = current;
        }
    }

    @Test
    public void multipleAsyncRequest() throws Exception {
        LatchingTaskHandler handler1 = new LatchingTaskHandler(10);
        LatchingTaskHandler handler2 = new LatchingTaskHandler(10);
        LatchingTaskHandler handler3 = new LatchingTaskHandler(10);

        BaseTask task1 = asyncOp.schedule(Collections.emptyMap(), handler1);
        assertThat(task1, notNullValue());
        assertFalse(task1.isRunning());
        task1.start();
        assertTrue(task1.isRunning());
        BaseTask task2 = asyncOp.schedule(Collections.emptyMap(), handler2);
        assertThat(task2, notNullValue());
        assertFalse(task2.isRunning());
        task2.start();
        assertTrue(task2.isRunning());
        BaseTask task3 = asyncOp.schedule(Collections.emptyMap(), handler3);
        assertThat(task3, notNullValue());
        assertFalse(task3.isRunning());
        task3.start();
        assertTrue(task3.isRunning());

        Sample result1 = handler1.getLastSample();
        assertThat(result1, notNullValue());
        assertThat(result1.getValue("event"), notNullValue());
        Sample result2 = handler2.getLastSample();
        assertThat(result2, notNullValue());
        assertThat(result2.getValue("event"), notNullValue());
        Sample result3 = handler3.getLastSample();
        assertThat(result3, notNullValue());
        assertThat(result3.getValue("event"), notNullValue());
    }

}
