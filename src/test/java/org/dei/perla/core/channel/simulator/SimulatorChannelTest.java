package org.dei.perla.core.channel.simulator;

import static org.hamcrest.CoreMatchers.both;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.number.OrderingComparison.greaterThanOrEqualTo;
import static org.hamcrest.number.OrderingComparison.lessThanOrEqualTo;
import static org.hamcrest.text.IsEmptyString.isEmptyOrNullString;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.dei.perla.core.channel.Channel;
import org.dei.perla.core.channel.IOHandler;
import org.dei.perla.core.channel.IORequest;
import org.dei.perla.core.channel.IOTask;
import org.dei.perla.core.channel.Payload;
import org.dei.perla.core.channel.SynchronizerIOHandler;
import org.dei.perla.core.fpc.descriptor.DeviceDescriptor;
import org.junit.BeforeClass;
import org.junit.Test;

public class SimulatorChannelTest {

    private static DeviceDescriptor device;
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
    }

    @Test
    public void parseSimulatorChannelTest() throws Exception {
        assertThat(device, notNullValue());
        assertThat(device.getChannelList().size(), equalTo(1));
        assertTrue(device.getChannelList().get(0) instanceof SimulatorChannelDescriptor);

        SimulatorChannelDescriptor descriptor = (SimulatorChannelDescriptor) device
                .getChannelList().get(0);

        // Test response parsing
        assertThat(descriptor.getResponseList().size(), equalTo(3));
        GeneratorFieldDescriptor field0;
        GeneratorFieldDescriptor field1;
        GeneratorFieldDescriptor field2;
        for (GeneratorDescriptor generator : descriptor.getResponseList()) {
            assertThat(generator.getId(), not(isEmptyOrNullString()));
            switch (generator.getId()) {
                case "temp-only":
                    assertThat(generator.getFieldList().size(), equalTo(2));

                    field0 = generator.getFieldList().get(0);
                    assertThat(field0.getName(), equalTo("type"));
                    assertThat(field0.getStrategy(),
                            equalTo(GeneratorFieldDescriptor.GeneratorFieldStrategy.STATIC));
                    assertThat(field0.getValue(), equalTo("temp-only"));

                    field1 = generator.getFieldList().get(1);
                    assertThat(field1.getName(), equalTo("temperature"));
                    assertThat(field1.getStrategy(),
                            equalTo(GeneratorFieldDescriptor.GeneratorFieldStrategy.DYNAMIC));
                    assertThat(field1.getMin(), equalTo(12));
                    assertThat(field1.getMax(), equalTo(32));
                    break;
                case "press-only":
                    assertThat(generator.getFieldList().size(), equalTo(2));

                    field0 = generator.getFieldList().get(0);
                    assertThat(field0.getName(), equalTo("type"));
                    assertThat(field0.getStrategy(),
                            equalTo(GeneratorFieldDescriptor.GeneratorFieldStrategy.STATIC));
                    assertThat(field0.getValue(), equalTo("press-only"));

                    field1 = generator.getFieldList().get(1);
                    assertThat(field1.getName(), equalTo("pressure"));
                    assertThat(field1.getStrategy(),
                            equalTo(GeneratorFieldDescriptor.GeneratorFieldStrategy.STEP));
                    assertThat(field1.getMin(), equalTo(450));
                    assertThat(field1.getMax(), equalTo(600));
                    assertThat(field1.getIncrement(), equalTo("1"));
                    break;
                case "all":
                    field0 = generator.getFieldList().get(0);
                    assertThat(field0.getName(), equalTo("type"));
                    assertThat(field0.getStrategy(),
                            equalTo(GeneratorFieldDescriptor.GeneratorFieldStrategy.STATIC));
                    assertThat(field0.getValue(), equalTo("all"));

                    field1 = generator.getFieldList().get(1);
                    assertThat(field1.getName(), equalTo("temperature"));
                    assertThat(field1.getStrategy(),
                            equalTo(GeneratorFieldDescriptor.GeneratorFieldStrategy.DYNAMIC));
                    assertThat(field1.getMin(), equalTo(12));
                    assertThat(field1.getMax(), equalTo(32));

                    field2 = generator.getFieldList().get(2);
                    assertThat(field2.getName(), equalTo("pressure"));
                    assertThat(field2.getStrategy(),
                            equalTo(GeneratorFieldDescriptor.GeneratorFieldStrategy.DYNAMIC));
                    assertThat(field2.getMin(), equalTo(450));
                    assertThat(field2.getMax(), equalTo(600));
                    break;
                default:
                    throw new RuntimeException("Unexpected response id "
                            + generator.getId());
            }
        }
    }

    @Test
    public void simulatorChannelFactoryTest() throws Exception {
        SimulatorChannelFactory factory = new SimulatorChannelFactory();
        Channel channel;

        assertThat(device, notNullValue());
        assertThat(device.getChannelList().size(), equalTo(1));
        assertTrue(device.getChannelList().get(0) instanceof SimulatorChannelDescriptor);
        channel = factory.createChannel(device.getChannelList().get(0));

        assertThat(channel, notNullValue());
        assertTrue(channel instanceof SimulatorChannel);
    }

    @Test
    public void tempOnlyRequestTest() throws Exception {
        SimulatorChannelFactory factory = new SimulatorChannelFactory();
        Channel channel = factory.createChannel(device.getChannelList().get(0));
        SimulatorIORequest request;
        IOTask task;
        Payload payload;
        SimulatorPayload simPayload;

        // temp-only request
        request = new SimulatorIORequest("temp-only-request", "temp-only");

        SynchronizerIOHandler syncHandler = new SynchronizerIOHandler();
        task = channel.submit(request, syncHandler);
        assertThat(task, notNullValue());
        payload = syncHandler.getResult().orElseThrow(RuntimeException::new);
        assertThat(payload, notNullValue());
        assertTrue(payload instanceof SimulatorPayload);
        simPayload = (SimulatorPayload) payload;

        for (Entry<String, Object> entry : simPayload.getValueMap().entrySet()) {
            switch (entry.getKey()) {
                case "type":
                    assertThat((String) entry.getValue(), equalTo("temp-only"));
                    break;
                case "temperature":
                    // Accounting for minor errors due to Floating point variable
                    // precision arithmetics
                    assertThat(
                            (Float) entry.getValue(),
                            both(greaterThanOrEqualTo(12f - 1)).and(
                                    lessThanOrEqualTo(32f + 1)));
                    break;
                default:
                    throw new RuntimeException(
                            "Unexpected message field for temp-only");
            }
        }
    }

    @Test
    public void pressOnlyRequestTest() throws Exception {
        SimulatorChannelFactory factory = new SimulatorChannelFactory();
        Channel channel = factory.createChannel(device.getChannelList().get(0));
        SimulatorIORequest request;
        IOTask task;
        Payload payload;
        SimulatorPayload simPayload;

        // press-only request
        request = new SimulatorIORequest("press-only-request", "press-only");
        SynchronizerIOHandler syncHandler = new SynchronizerIOHandler();
        task = channel.submit(request, syncHandler);
        assertThat(task, notNullValue());
        payload = syncHandler.getResult().orElseThrow(RuntimeException::new);
        assertThat(payload, notNullValue());
        assertTrue(payload instanceof SimulatorPayload);
        simPayload = (SimulatorPayload) payload;

        for (Entry<String, Object> entry : simPayload.getValueMap().entrySet()) {
            switch (entry.getKey()) {
                case "type":
                    assertThat((String) entry.getValue(), equalTo("press-only"));
                    break;
                case "pressure":
                    // Accounting for minor errors due to Floating point variable
                    // precision arithmetics
                    assertThat(
                            (Float) entry.getValue(),
                            both(greaterThanOrEqualTo(450f - 1)).and(
                                    lessThanOrEqualTo(600f + 1)));
                    break;
                default:
                    throw new RuntimeException(
                            "Unexpected message field for temp-only");
            }
        }
    }

    @Test
    public void allRequestTest() throws Exception {
        SimulatorChannelFactory factory = new SimulatorChannelFactory();
        Channel channel = factory.createChannel(device.getChannelList().get(0));
        SimulatorIORequest request;
        IOTask task;
        Payload payload;
        SimulatorPayload simPayload;

        // press-only request
        request = new SimulatorIORequest("all-request", "all");
        SynchronizerIOHandler syncHandler = new SynchronizerIOHandler();
        task = channel.submit(request, syncHandler);
        assertThat(task, notNullValue());
        payload = syncHandler.getResult().orElseThrow(RuntimeException::new);
        assertThat(payload, notNullValue());
        assertTrue(payload instanceof SimulatorPayload);
        simPayload = (SimulatorPayload) payload;

        for (Entry<String, Object> entry : simPayload.getValueMap().entrySet()) {
            switch (entry.getKey()) {
                case "type":
                    assertThat((String) entry.getValue(), equalTo("all"));
                    break;
                case "pressure":
                    // Accounting for minor errors due to Floating point variable
                    // precision arithmetics
                    assertThat(
                            (Float) entry.getValue(),
                            both(greaterThanOrEqualTo(450f - 1)).and(
                                    lessThanOrEqualTo(600f + 1)));
                    break;
                case "temperature":
                    // Accounting for minor errors due to Floating point variable
                    // precision arithmetics
                    assertThat(
                            (Float) entry.getValue(),
                            both(greaterThanOrEqualTo(12f - 1)).and(
                                    lessThanOrEqualTo(32f + 1)));
                    break;
                case "timestamp":
                    assertTrue(entry.getValue() instanceof ZonedDateTime);
                    break;
                default:
                    throw new RuntimeException(
                            "Unexpected message field for temp-only");
            }
        }
    }

    @Test
    public void periodicTemperatureTest() throws Exception {
        SimulatorChannelFactory factory = new SimulatorChannelFactory();
        Channel channel = factory.createChannel(device.getChannelList().get(0));
        TestIOHandler handler = new TestIOHandler();
        channel.setAsyncIOHandler(handler);
        SimulatorIORequest request;
        Map<String, Object> valueMap = new HashMap<>();
        SimulatorPayload period;
        SynchronizerIOHandler syncHandler = new SynchronizerIOHandler();

        // 100 msec
        valueMap.put("period", 100);
        period = new SimulatorPayload(valueMap);
        request = new SimulatorIORequest("periodic-temp-only-request",
                "temp-only");
        request.setParameter("period", period);

        channel.submit(request, syncHandler);
        syncHandler.getResult();
        Thread.sleep(1500);
        assertThat(handler.getCount(), greaterThanOrEqualTo(10));

        // 10 msec
        valueMap.put("period", 10);
        period = new SimulatorPayload(valueMap);
        request = new SimulatorIORequest("periodic-temp-only-request",
                "temp-only");
        request.setParameter("period", period);
        syncHandler = new SynchronizerIOHandler();
        channel.submit(request, syncHandler);
        syncHandler.getResult();
        handler.reset();
        Thread.sleep(150);
        assertThat(handler.getCount(), greaterThanOrEqualTo(10));

        // Stop sampling
        valueMap.put("period", 0);
        period = new SimulatorPayload(valueMap);
        request = new SimulatorIORequest("periodic-temp-only-request",
                "temp-only");
        request.setParameter("period", period);
        syncHandler = new SynchronizerIOHandler();
        channel.submit(request, syncHandler);
        syncHandler.getResult();
        handler.reset();
        Thread.sleep(150);
        assertThat(handler.getCount(), equalTo(0));
    }

    private static class TestIOHandler implements IOHandler {

        private volatile int count = 0;

        public int getCount() {
            return count;
        }

        public void reset() {
            count = 0;
        }

        @Override
        public void complete(IORequest request, Optional<Payload> result) {

            count++;
            assertThat(result, notNullValue());
            assertTrue(result.isPresent());
            assertTrue(result.get() instanceof SimulatorPayload);
            SimulatorPayload pld = (SimulatorPayload) result.get();
            for (Entry<String, Object> entry : pld.getValueMap().entrySet()) {
                switch (entry.getKey()) {
                    case "type":
                        assertThat((String) entry.getValue(), equalTo("temp-only"));
                        break;
                    case "temperature":
                        // Accounting for minor errors due to Floating point
                        // variable
                        // precision arithmetics
                        assertThat(
                                (Float) entry.getValue(),
                                both(greaterThanOrEqualTo(12f - 1)).and(
                                        lessThanOrEqualTo(32f + 1)));
                        break;
                    default:
                        throw new RuntimeException(
                                "Unexpected message field for temp-only");
                }
            }
        }

        @Override
        public void error(IORequest request, Throwable cause) {

        }

    }

}
