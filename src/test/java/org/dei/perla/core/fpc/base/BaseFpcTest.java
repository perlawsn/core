package org.dei.perla.core.fpc.base;

import org.dei.perla.core.channel.ChannelFactory;
import org.dei.perla.core.channel.IORequestBuilderFactory;
import org.dei.perla.core.channel.simulator.SimulatorChannelFactory;
import org.dei.perla.core.channel.simulator.SimulatorIORequestBuilderFactory;
import org.dei.perla.core.channel.simulator.SimulatorMapperFactory;
import org.dei.perla.core.descriptor.DeviceDescriptor;
import org.dei.perla.core.descriptor.JaxbDeviceDescriptorParser;
import org.dei.perla.core.fpc.DataType;
import org.dei.perla.core.fpc.Fpc;
import org.dei.perla.core.fpc.FpcFactory;
import org.dei.perla.core.fpc.Task;
import org.dei.perla.core.message.MapperFactory;
import org.dei.perla.core.sample.Attribute;
import org.dei.perla.core.sample.Sample;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.FileInputStream;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class BaseFpcTest {

    private static final String descriptorPath =
            "src/test/java/org/dei/perla/core/fpc/base/fpc_descriptor.xml";
    private static Fpc fpc;

    private static final Set<String> packages;
    static {
        Set<String> pkgs = new HashSet<>();
        pkgs.add("org.dei.perla.core.descriptor");
        pkgs.add("org.dei.perla.core.descriptor.instructions");
        pkgs.add("org.dei.perla.core.channel.simulator");
        packages = Collections.unmodifiableSet(pkgs);
    }

    @BeforeClass
    public static void createFpc() throws Exception {
        JaxbDeviceDescriptorParser parser =
                new JaxbDeviceDescriptorParser(packages);

        List<MapperFactory> mapperFactoryList = new ArrayList<>();
        mapperFactoryList.add(new SimulatorMapperFactory());
        List<ChannelFactory> channelFactoryList = new ArrayList<>();
        channelFactoryList.add(new SimulatorChannelFactory());
        List<IORequestBuilderFactory> requestBuilderFactoryList = new ArrayList<>();
        requestBuilderFactoryList.add(new SimulatorIORequestBuilderFactory());
        FpcFactory fpcFactory = new BaseFpcFactory(mapperFactoryList,
                channelFactoryList, requestBuilderFactoryList);

        DeviceDescriptor desc = parser
                .parse(new FileInputStream(descriptorPath));
        fpc = fpcFactory.createFpc(desc, 1);
    }

    @Test
    public void testGetOperation() throws InterruptedException,
            ExecutionException {
        List<Attribute> attributeList;
        LatchingTaskHandler handler;
        Sample sample;

        // integer-get
        attributeList = new ArrayList<>();
        attributeList.add(Attribute.create("integer", DataType.INTEGER));
        handler = new LatchingTaskHandler(1);
        fpc.get(attributeList, handler);
        sample = handler.getLastSample();

        assertThat(sample, notNullValue());
        assertThat(sample.getValue("integer"), notNullValue());
        assertTrue(sample.getValue("integer") instanceof Integer);
        // Check if the Fpc is adding the timestamp
        assertThat(sample.getValue("timestamp"), notNullValue());
        assertTrue(sample.getValue("timestamp") instanceof Instant);

        // string-get
        attributeList.clear();
        attributeList.add(Attribute.create("string", DataType.STRING));
        handler = new LatchingTaskHandler(1);
        fpc.get(attributeList, handler);
        sample = handler.getLastSample();

        assertThat(sample, notNullValue());
        assertThat(sample.getValue("string"), notNullValue());
        assertTrue(sample.getValue("string") instanceof String);
        // Check if the Fpc is adding the timestamp
        assertThat(sample.getValue("timestamp"), notNullValue());
        assertTrue(sample.getValue("timestamp") instanceof Instant);
    }

    @Test
    public void testMixedStaticDynamicGet() throws InterruptedException,
            ExecutionException {
        List<Attribute> attributeList;
        LatchingTaskHandler handler;
        Sample sample;

        attributeList = new ArrayList<>();
        attributeList.add(Attribute.create("integer", DataType.INTEGER));
        attributeList.add(Attribute.create("static", DataType.INTEGER));
        handler = new LatchingTaskHandler(1);
        fpc.get(attributeList, handler);
        sample = handler.getLastSample();

        assertThat(sample, notNullValue());
        assertThat(sample.getValue("integer"), notNullValue());
        assertTrue(sample.getValue("integer") instanceof Integer);
        // Check if the Fpc is adding the timestamp
        assertThat(sample.getValue("timestamp"), notNullValue());
        assertTrue(sample.getValue("timestamp") instanceof Instant);
        // Check if the Fpc is adding the requested static attribute
        assertThat(sample.getValue("static"), notNullValue());
        assertTrue(sample.getValue("static") instanceof Integer);
        Integer value = (Integer) sample.getValue("static");
        assertThat(value, equalTo(5));
    }

    @Test
    public void testStaticGet() throws InterruptedException, ExecutionException {
        List<Attribute> attributeList;
        LatchingTaskHandler handler;
        Sample sample;

        attributeList = new ArrayList<>();
        attributeList.add(Attribute.create("static", DataType.INTEGER));
        handler = new LatchingTaskHandler(1);
        fpc.get(attributeList, handler);
        sample = handler.getLastSample();

        assertThat(sample, notNullValue());
        // Check if the Fpc is adding the timestamp
        assertThat(sample.getValue("timestamp"), notNullValue());
        assertTrue(sample.getValue("timestamp") instanceof Instant);
        // Check if the Fpc is adding the requested static attribute
        assertThat(sample.getValue("static"), notNullValue());
        assertTrue(sample.getValue("static") instanceof Integer);
        Integer value = (Integer) sample.getValue("static");
        assertThat(value, equalTo(5));
    }

    @Test
    public void testPeriodicOperation() throws InterruptedException,
            ExecutionException {
        List<Attribute> attributeList;
        Sample sample;

        // Request string and integer
        attributeList = new ArrayList<>();
        attributeList.add(Attribute.create("string", DataType.STRING));
        attributeList.add(Attribute.create("integer", DataType.INTEGER));
        LatchingTaskHandler handler1 = new LatchingTaskHandler(100);
        Task task1 = fpc.get(attributeList, 10, handler1);

        assertThat(task1, notNullValue());
        assertTrue(task1 instanceof PeriodicTask);
        handler1.awaitCompletion();
        sample = handler1.getLastSample();
        assertThat(sample, notNullValue());
        assertThat(sample.getValue("string"), notNullValue());
        assertTrue(sample.getValue("string") instanceof String);
        assertThat(sample.getValue("integer"), notNullValue());
        assertTrue(sample.getValue("integer") instanceof Integer);
        // Check if the Fpc is adding the timestamp
        assertThat(sample.getValue("timestamp"), notNullValue());
        assertTrue(sample.getValue("timestamp") instanceof Instant);

        // Request string and float
        attributeList = new ArrayList<>();
        attributeList.add(Attribute.create("string", DataType.STRING));
        attributeList.add(Attribute.create("float", DataType.FLOAT));
        LatchingTaskHandler handler2 = new LatchingTaskHandler(1000);
        Task task2 = fpc.get(attributeList, 1, handler2);

        assertThat(task2, notNullValue());
        assertTrue(task2 instanceof PeriodicTask);
        handler2.awaitCompletion();
        sample = handler1.getLastSample();
        assertThat(sample, notNullValue());
        assertThat(sample.getValue("string"), notNullValue());
        assertTrue(sample.getValue("string") instanceof String);
        assertThat(sample.getValue("float"), notNullValue());
        assertTrue(sample.getValue("float") instanceof Float);
        // Check if the Fpc is adding the timestamp
        assertThat(sample.getValue("timestamp"), notNullValue());
        assertTrue(sample.getValue("timestamp") instanceof Instant);

        // Check if both tasks are backed by the same Operation
        PeriodicTask pTask1 = (PeriodicTask) task1;
        PeriodicTask pTask2 = (PeriodicTask) task2;
        assertThat(pTask1.getOperation(), equalTo(pTask2.getOperation()));

        // Stop one task and check the other still runs as it should
        task2.stop();
        int countBefore = handler1.getCount();
        Thread.sleep(500);
        assertThat(countBefore, lessThan(handler1.getCount()));

        task1.stop();
    }

    @Test
    public void testMixedStaticDynamicPeriodic() throws InterruptedException,
            ExecutionException {
        List<Attribute> attributeList;
        Sample sample;

        // Request string and integer
        attributeList = new ArrayList<>();
        attributeList.add(Attribute.create("string", DataType.STRING));
        attributeList.add(Attribute.create("integer", DataType.INTEGER));
        attributeList.add(Attribute.create("static", DataType.INTEGER));
        LatchingTaskHandler handler1 = new LatchingTaskHandler(1);
        Task task = fpc.get(attributeList, 10, handler1);

        assertThat(task, notNullValue());
        assertTrue(task instanceof PeriodicTask);
        sample = handler1.getLastSample();
        assertThat(sample, notNullValue());
        assertThat(sample.getValue("string"), notNullValue());
        assertTrue(sample.getValue("string") instanceof String);
        assertThat(sample.getValue("integer"), notNullValue());
        assertTrue(sample.getValue("integer") instanceof Integer);
        // Check if the Fpc is adding the timestamp
        assertThat(sample.getValue("timestamp"), notNullValue());
        assertTrue(sample.getValue("timestamp") instanceof Instant);
        // Check if the Fpc is adding the requested static attribute
        assertThat(sample.getValue("static"), notNullValue());
        assertTrue(sample.getValue("static") instanceof Integer);
        Integer value = (Integer) sample.getValue("static");
        assertThat(value, equalTo(5));
        task.stop();
    }

    @Test
    public void testStaticPeriodic() throws InterruptedException,
            ExecutionException {
        List<Attribute> attributeList;
        Sample sample;

        attributeList = new ArrayList<>();
        attributeList.add(Attribute.create("static", DataType.INTEGER));
        LatchingTaskHandler handler = new LatchingTaskHandler(100);
        Task task = fpc.get(attributeList, 10, handler);

        assertThat(task, notNullValue());
        assertTrue(task instanceof StaticPeriodicTask);
        handler.awaitCompletion();
        sample = handler.getLastSample();
        assertThat(sample, notNullValue());
        // Check if the Fpc is adding the timestamp
        assertThat(sample.getValue("timestamp"), notNullValue());
        assertTrue(sample.getValue("timestamp") instanceof Instant);
        // Check if the Fpc is adding the requested static attribute
        assertThat(sample.getValue("static"), notNullValue());
        assertTrue(sample.getValue("static") instanceof Integer);
        Integer value = (Integer) sample.getValue("static");
        assertThat(value, equalTo(5));
    }

    @Test
    public void testPeriodicMultipleHandler() throws InterruptedException,
            ExecutionException {
        List<Attribute> attributeList;
        Sample sample;

        // Request string and integer
        attributeList = new ArrayList<>();
        attributeList.add(Attribute.create("boolean", DataType.BOOLEAN));
        attributeList.add(Attribute.create("integer", DataType.INTEGER));
        LatchingTaskHandler handler1 = new LatchingTaskHandler(100);
        Task task1 = fpc.get(attributeList, 10, handler1);

        assertThat(task1, notNullValue());
        assertTrue(task1 instanceof PeriodicTask);
        handler1.awaitCompletion();
        sample = handler1.getLastSample();
        assertThat(sample, notNullValue());
        assertThat(sample.getValue("boolean"), notNullValue());
        assertTrue(sample.getValue("boolean") instanceof Boolean);
        assertThat(sample.getValue("integer"), notNullValue());
        assertTrue(sample.getValue("integer") instanceof Integer);
        // Check if the Fpc is adding the timestamp
        assertThat(sample.getValue("timestamp"), notNullValue());
        assertTrue(sample.getValue("timestamp") instanceof Instant);
    }

    @Test
    public void testSetOperation() throws InterruptedException,
            ExecutionException {
        Map<Attribute, Object> valueMap = new HashMap<>();
        valueMap.put(Attribute.create("integer", DataType.INTEGER), 8);
        LatchingTaskHandler handler = new LatchingTaskHandler(1);
        Task task = fpc.set(valueMap, handler);
        assertThat(task, notNullValue());

        handler.awaitCompletion();
    }

    @Test
    public void testPeriodicAsyncOperation() throws InterruptedException,
            ExecutionException {
        List<Attribute> attributeList;
        Sample sample;

        // Request the async event
        attributeList = new ArrayList<>();
        attributeList.add(Attribute.create("event", DataType.INTEGER));
        LatchingTaskHandler handler = new LatchingTaskHandler(2);
        Task task = fpc.get(attributeList, 500, handler);

        assertThat(task, notNullValue());
        assertTrue(task instanceof PeriodicTask);
        sample = handler.getLastSample();
        assertThat(sample, notNullValue());
        assertThat(sample.getValue("event"), notNullValue());
        assertTrue(sample.getValue("event") instanceof Integer);
        // Check if the Fpc is adding the timestamp
        assertThat(sample.getValue("timestamp"), notNullValue());
        assertTrue(sample.getValue("timestamp") instanceof Instant);
    }

    @Test
    public void testOneoffAsyncOperation() throws InterruptedException,
            ExecutionException {
        List<Attribute> attributeList;
        Sample sample;

        // Request the async event
        attributeList = new ArrayList<>();
        attributeList.add(Attribute.create("event", DataType.INTEGER));
        LatchingTaskHandler handler = new LatchingTaskHandler(1);
        Task task = fpc.get(attributeList, handler);

        assertThat(task, notNullValue());
        assertFalse(task instanceof PeriodicTask);
        sample = handler.getLastSample();
        assertThat(sample, notNullValue());
    }

    @Test
    public void testAsyncOperation() throws InterruptedException,
            ExecutionException {
        List<Attribute> attributeList;
        Sample sample;

        // Request the async event
        attributeList = new ArrayList<>();
        attributeList.add(Attribute.create("event", DataType.INTEGER));
        LatchingTaskHandler handler = new LatchingTaskHandler(5);
        Task task = fpc.async(attributeList, handler);

        assertThat(task, notNullValue());
        sample = handler.getLastSample();
        assertThat(sample, notNullValue());
        assertThat(sample.getValue("event"), notNullValue());
        assertTrue(sample.getValue("event") instanceof Integer);
        // Check if the Fpc is adding the timestamp
        assertThat(sample.getValue("timestamp"), notNullValue());
        assertTrue(sample.getValue("timestamp") instanceof Instant);
    }

    @Test
    public void testSchedulerPolicy() throws Exception {
        Attribute intAtt = Attribute.create("integer", DataType.INTEGER);
        Attribute boolAtt = Attribute.create("boolean", DataType.BOOLEAN);
        List<Attribute> atts = Arrays.asList(new Attribute[] {
                intAtt,
                boolAtt
        });
        LatchingTaskHandler h = new LatchingTaskHandler(1);
        Sample sample;

        Task task = fpc.get(atts, h);
        assertThat(task, notNullValue());
        sample = h.getLastSample();
        assertThat(sample, notNullValue());
        assertTrue(sample.fields().contains(intAtt));
        assertThat(sample.getValue("integer"), notNullValue());
        assertTrue(sample.fields().contains(boolAtt));
        assertThat(sample.getValue("boolean"), nullValue());

        h = new LatchingTaskHandler(1);
        task = fpc.get(atts, true, h);
        assertThat(task, nullValue());
    }

}
