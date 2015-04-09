package org.dei.perla.core.fpc.base;

import org.dei.perla.core.channel.ChannelFactory;
import org.dei.perla.core.channel.IORequestBuilderFactory;
import org.dei.perla.core.channel.simulator.SimulatorChannelFactory;
import org.dei.perla.core.channel.simulator.SimulatorIORequestBuilderFactory;
import org.dei.perla.core.channel.simulator.SimulatorMapperFactory;
import org.dei.perla.core.descriptor.DataType;
import org.dei.perla.core.descriptor.DeviceDescriptor;
import org.dei.perla.core.descriptor.JaxbDeviceDescriptorParser;
import org.dei.perla.core.fpc.Fpc;
import org.dei.perla.core.fpc.FpcFactory;
import org.dei.perla.core.fpc.Task;
import org.dei.perla.core.message.MapperFactory;
import org.dei.perla.core.record.Attribute;
import org.dei.perla.core.record.Record;
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

	@BeforeClass
	public static void createFpc() throws Exception {
		List<String> packageList = new ArrayList<>();
		packageList.add("org.dei.perla.core.descriptor");
		packageList.add("org.dei.perla.core.descriptor.instructions");
		packageList.add("org.dei.perla.core.channel.simulator");
		JaxbDeviceDescriptorParser parser = new JaxbDeviceDescriptorParser(
				packageList);

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
		Record record;

		// integer-get
		attributeList = new ArrayList<>();
		attributeList.add(Attribute.create("integer", DataType.INTEGER));
		handler = new LatchingTaskHandler(1);
		fpc.get(attributeList, handler);
		record = handler.getLastSample();

		assertThat(record, notNullValue());
		assertThat(record.getValue("integer"), notNullValue());
		assertTrue(record.getValue("integer") instanceof Integer);
		// Check if the Fpc is adding the timestamp
		assertThat(record.getValue("timestamp"), notNullValue());
		assertTrue(record.getValue("timestamp") instanceof Instant);

		// string-get
		attributeList.clear();
		attributeList.add(Attribute.create("string", DataType.STRING));
		handler = new LatchingTaskHandler(1);
		fpc.get(attributeList, handler);
		record = handler.getLastSample();

		assertThat(record, notNullValue());
		assertThat(record.getValue("string"), notNullValue());
		assertTrue(record.getValue("string") instanceof String);
		// Check if the Fpc is adding the timestamp
		assertThat(record.getValue("timestamp"), notNullValue());
		assertTrue(record.getValue("timestamp") instanceof Instant);
	}

	@Test
	public void testMixedStaticDynamicGet() throws InterruptedException,
			ExecutionException {
		List<Attribute> attributeList;
		LatchingTaskHandler handler;
		Record record;

		attributeList = new ArrayList<>();
		attributeList.add(Attribute.create("integer", DataType.INTEGER));
		attributeList.add(Attribute.create("static", DataType.INTEGER));
		handler = new LatchingTaskHandler(1);
		fpc.get(attributeList, handler);
		record = handler.getLastSample();

		assertThat(record, notNullValue());
		assertThat(record.getValue("integer"), notNullValue());
		assertTrue(record.getValue("integer") instanceof Integer);
		// Check if the Fpc is adding the timestamp
		assertThat(record.getValue("timestamp"), notNullValue());
		assertTrue(record.getValue("timestamp") instanceof Instant);
		// Check if the Fpc is adding the requested static attribute
		assertThat(record.getValue("static"), notNullValue());
		assertTrue(record.getValue("static") instanceof Integer);
		Integer value = (Integer) record.getValue("static");
		assertThat(value, equalTo(5));
	}

	@Test
	public void testStaticGet() throws InterruptedException, ExecutionException {
		List<Attribute> attributeList;
		LatchingTaskHandler handler;
		Record record;

		attributeList = new ArrayList<>();
		attributeList.add(Attribute.create("static", DataType.INTEGER));
		handler = new LatchingTaskHandler(1);
		fpc.get(attributeList, handler);
		record = handler.getLastSample();

		assertThat(record, notNullValue());
		// Check if the Fpc is adding the timestamp
		assertThat(record.getValue("timestamp"), notNullValue());
		assertTrue(record.getValue("timestamp") instanceof Instant);
		// Check if the Fpc is adding the requested static attribute
		assertThat(record.getValue("static"), notNullValue());
		assertTrue(record.getValue("static") instanceof Integer);
		Integer value = (Integer) record.getValue("static");
		assertThat(value, equalTo(5));
	}

	@Test
	public void testPeriodicOperation() throws InterruptedException,
			ExecutionException {
		List<Attribute> attributeList;
		Record record;

		// Request string and integer
		attributeList = new ArrayList<>();
		attributeList.add(Attribute.create("string", DataType.STRING));
		attributeList.add(Attribute.create("integer", DataType.INTEGER));
		LatchingTaskHandler handler1 = new LatchingTaskHandler(100);
		Task task1 = fpc.get(attributeList, 10, handler1);

		assertThat(task1, notNullValue());
		assertTrue(task1 instanceof PeriodicTask);
		assertThat(handler1.getAveragePeriod(), greaterThanOrEqualTo(9.9d));
		assertThat(handler1.getAveragePeriod(), lessThan(11d));
		record = handler1.getLastSample();
		assertThat(record, notNullValue());
		assertThat(record.getValue("string"), notNullValue());
		assertTrue(record.getValue("string") instanceof String);
		assertThat(record.getValue("integer"), notNullValue());
		assertTrue(record.getValue("integer") instanceof Integer);
		// Check if the Fpc is adding the timestamp
		assertThat(record.getValue("timestamp"), notNullValue());
		assertTrue(record.getValue("timestamp") instanceof Instant);

		// Request string and float
		attributeList = new ArrayList<>();
		attributeList.add(Attribute.create("string", DataType.STRING));
		attributeList.add(Attribute.create("float", DataType.FLOAT));
		LatchingTaskHandler handler2 = new LatchingTaskHandler(1000);
		Task task2 = fpc.get(attributeList, 1, handler2);

		assertThat(task2, notNullValue());
		assertTrue(task2 instanceof PeriodicTask);
		assertThat(handler2.getAveragePeriod(), greaterThanOrEqualTo(1d));
		assertThat(handler2.getAveragePeriod(), lessThan(1.1d));
		assertThat(handler1.getAveragePeriod(), greaterThanOrEqualTo(9.9d));
		assertThat(handler1.getAveragePeriod(), lessThan(11d));
		record = handler1.getLastSample();
		assertThat(record, notNullValue());
		assertThat(record.getValue("string"), notNullValue());
		assertTrue(record.getValue("string") instanceof String);
		assertThat(record.getValue("float"), notNullValue());
		assertTrue(record.getValue("float") instanceof Float);
		// Check if the Fpc is adding the timestamp
		assertThat(record.getValue("timestamp"), notNullValue());
		assertTrue(record.getValue("timestamp") instanceof Instant);

		// Check if both tasks are backed by the same Operation
		PeriodicTask pTask1 = (PeriodicTask) task1;
		PeriodicTask pTask2 = (PeriodicTask) task2;
		assertThat(pTask1.getOperation(), equalTo(pTask2.getOperation()));

		// Stop one task and check the other still runs as it should
		task2.stop();
		int countBefore = handler1.getCount();
		Thread.sleep(500);
		assertThat(countBefore, lessThan(handler1.getCount()));
		assertThat(handler1.getAveragePeriod(), greaterThanOrEqualTo(9.9d));
		assertThat(handler1.getAveragePeriod(), lessThan(11d));

		task1.stop();
	}

	@Test
	public void testMixedStaticDynamicPeriodic() throws InterruptedException,
			ExecutionException {
		List<Attribute> attributeList;
		Record record;

		// Request string and integer
		attributeList = new ArrayList<>();
		attributeList.add(Attribute.create("string", DataType.STRING));
		attributeList.add(Attribute.create("integer", DataType.INTEGER));
		attributeList.add(Attribute.create("static", DataType.INTEGER));
		LatchingTaskHandler handler1 = new LatchingTaskHandler(1);
		Task task = fpc.get(attributeList, 10, handler1);

		assertThat(task, notNullValue());
		assertTrue(task instanceof PeriodicTask);
		record = handler1.getLastSample();
		assertThat(record, notNullValue());
		assertThat(record.getValue("string"), notNullValue());
		assertTrue(record.getValue("string") instanceof String);
		assertThat(record.getValue("integer"), notNullValue());
		assertTrue(record.getValue("integer") instanceof Integer);
		// Check if the Fpc is adding the timestamp
		assertThat(record.getValue("timestamp"), notNullValue());
		assertTrue(record.getValue("timestamp") instanceof Instant);
		// Check if the Fpc is adding the requested static attribute
		assertThat(record.getValue("static"), notNullValue());
		assertTrue(record.getValue("static") instanceof Integer);
		Integer value = (Integer) record.getValue("static");
		assertThat(value, equalTo(5));
		task.stop();
	}

	@Test
	public void testStaticPeriodic() throws InterruptedException,
			ExecutionException {
		List<Attribute> attributeList;
		Record record;

		attributeList = new ArrayList<>();
		attributeList.add(Attribute.create("static", DataType.INTEGER));
		LatchingTaskHandler handler = new LatchingTaskHandler(100);
		Task task = fpc.get(attributeList, 10, handler);

		assertThat(task, notNullValue());
		assertTrue(task instanceof PeriodicTask);
		assertThat(handler.getAveragePeriod(), greaterThanOrEqualTo(9.9d));
		assertThat(handler.getAveragePeriod(), lessThan(11d));
		record = handler.getLastSample();
		assertThat(record, notNullValue());
		// Check if the Fpc is adding the timestamp
		assertThat(record.getValue("timestamp"), notNullValue());
		assertTrue(record.getValue("timestamp") instanceof Instant);
		// Check if the Fpc is adding the requested static attribute
		assertThat(record.getValue("static"), notNullValue());
		assertTrue(record.getValue("static") instanceof Integer);
		Integer value = (Integer) record.getValue("static");
		assertThat(value, equalTo(5));
	}

	@Test
	public void testPeriodicMultipleHandler() throws InterruptedException,
			ExecutionException {
		List<Attribute> attributeList;
		Record record;

		// Request string and integer
		attributeList = new ArrayList<>();
		attributeList.add(Attribute.create("boolean", DataType.BOOLEAN));
		attributeList.add(Attribute.create("integer", DataType.INTEGER));
		LatchingTaskHandler handler1 = new LatchingTaskHandler(100);
		Task task1 = fpc.get(attributeList, 10, handler1);

		assertThat(task1, notNullValue());
		assertTrue(task1 instanceof PeriodicTask);
		assertThat(handler1.getAveragePeriod(), greaterThanOrEqualTo(9.8d));
		assertThat(handler1.getAveragePeriod(), lessThan(11d));
		record = handler1.getLastSample();
		assertThat(record, notNullValue());
		assertThat(record.getValue("boolean"), notNullValue());
		assertTrue(record.getValue("boolean") instanceof Boolean);
		assertThat(record.getValue("integer"), notNullValue());
		assertTrue(record.getValue("integer") instanceof Integer);
		// Check if the Fpc is adding the timestamp
		assertThat(record.getValue("timestamp"), notNullValue());
		assertTrue(record.getValue("timestamp") instanceof Instant);
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
		Record record;

		// Request the async event
		attributeList = new ArrayList<>();
		attributeList.add(Attribute.create("event", DataType.INTEGER));
		LatchingTaskHandler handler = new LatchingTaskHandler(2);
		Task task = fpc.get(attributeList, 500, handler);

		assertThat(task, notNullValue());
		assertTrue(task instanceof PeriodicTask);
		record = handler.getLastSample();
		assertThat(record, notNullValue());
		assertThat(record.getValue("event"), notNullValue());
		assertTrue(record.getValue("event") instanceof Integer);
		// Check if the Fpc is adding the timestamp
		assertThat(record.getValue("timestamp"), notNullValue());
		assertTrue(record.getValue("timestamp") instanceof Instant);
	}

	@Test
	public void testOneoffAsyncOperation() throws InterruptedException,
			ExecutionException {
		List<Attribute> attributeList;
		Record record;

		// Request the async event
		attributeList = new ArrayList<>();
		attributeList.add(Attribute.create("event", DataType.INTEGER));
		LatchingTaskHandler handler = new LatchingTaskHandler(1);
		Task task = fpc.get(attributeList, handler);

		assertThat(task, notNullValue());
		assertFalse(task instanceof PeriodicTask);
		record = handler.getLastSample();
		assertThat(record, notNullValue());
	}

	@Test
	public void testAsyncOperation() throws InterruptedException,
			ExecutionException {
		List<Attribute> attributeList;
		Record record;

		// Request the async event
		attributeList = new ArrayList<>();
		attributeList.add(Attribute.create("event", DataType.INTEGER));
		LatchingTaskHandler handler = new LatchingTaskHandler(5);
		Task task = fpc.async(attributeList, handler);

		assertThat(task, notNullValue());
		record = handler.getLastSample();
		assertThat(record, notNullValue());
		assertThat(record.getValue("event"), notNullValue());
		assertTrue(record.getValue("event") instanceof Integer);
		// Check if the Fpc is adding the timestamp
		assertThat(record.getValue("timestamp"), notNullValue());
		assertTrue(record.getValue("timestamp") instanceof Instant);
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
		Record record;

		Task task = fpc.get(atts, h);
		assertThat(task, notNullValue());
		record = h.getLastSample();
		assertThat(record, notNullValue());
		assertTrue(record.fields().contains(intAtt));
		assertThat(record.getValue("integer"), notNullValue());
		assertTrue(record.fields().contains(boolAtt));
		assertThat(record.getValue("boolean"), nullValue());

		h = new LatchingTaskHandler(1);
		task = fpc.get(atts, true, h);
		assertThat(task, nullValue());
	}

}
