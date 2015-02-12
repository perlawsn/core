package org.dei.perla.core.fpc.base;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.FileInputStream;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.dei.perla.core.channel.ChannelFactory;
import org.dei.perla.core.channel.IORequestBuilderFactory;
import org.dei.perla.core.channel.simulator.SimulatorChannelFactory;
import org.dei.perla.core.channel.simulator.SimulatorIORequestBuilderFactory;
import org.dei.perla.core.channel.simulator.SimulatorMapperFactory;
import org.dei.perla.core.fpc.Attribute;
import org.dei.perla.core.fpc.Fpc;
import org.dei.perla.core.fpc.FpcFactory;
import org.dei.perla.core.fpc.Task;
import org.dei.perla.core.fpc.descriptor.DataType;
import org.dei.perla.core.fpc.descriptor.DeviceDescriptor;
import org.dei.perla.core.fpc.descriptor.JaxbDeviceDescriptorParser;
import org.dei.perla.core.fpc.engine.Record;
import org.dei.perla.core.message.MapperFactory;
import org.junit.BeforeClass;
import org.junit.Test;

public class BaseFpcTest {

	private static final String descriptorPath =
            "src/test/java/org/dei/perla/core/fpc/base/fpc_descriptor.xml";
	private static Fpc fpc;

	@BeforeClass
	public static void createFpc() throws Exception {
		List<String> packageList = new ArrayList<>();
		packageList.add("org.dei.perla.core.fpc.descriptor");
		packageList.add("org.dei.perla.core.fpc.descriptor.instructions");
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
		SynchronizerTaskHandler handler;
		Record record;

		// integer-get
		attributeList = new ArrayList<>();
		attributeList.add(Attribute.create("integer", DataType.INTEGER));
		handler = new SynchronizerTaskHandler();
		fpc.get(attributeList, handler);
		record = handler.getResult();

		assertThat(record, notNullValue());
		assertThat(record.get("integer"), notNullValue());
		assertTrue(record.get("integer") instanceof Integer);
		// Check if the Fpc is adding the timestamp
		assertThat(record.get("timestamp"), notNullValue());
		assertTrue(record.get("timestamp") instanceof ZonedDateTime);

		// string-get
		attributeList.clear();
		attributeList.add(Attribute.create("string", DataType.STRING));
		handler = new SynchronizerTaskHandler();
		fpc.get(attributeList, handler);
		record = handler.getResult();

		assertThat(record, notNullValue());
		assertThat(record.get("string"), notNullValue());
		assertTrue(record.get("string") instanceof String);
		// Check if the Fpc is adding the timestamp
		assertThat(record.get("timestamp"), notNullValue());
		assertTrue(record.get("timestamp") instanceof ZonedDateTime);
	}

	@Test
	public void testMixedStaticDynamicGet() throws InterruptedException,
			ExecutionException {
		List<Attribute> attributeList;
		SynchronizerTaskHandler handler;
		Record record;

		attributeList = new ArrayList<>();
		attributeList.add(Attribute.create("integer", DataType.INTEGER));
		attributeList.add(Attribute.create("static", DataType.INTEGER));
		handler = new SynchronizerTaskHandler();
		fpc.get(attributeList, handler);
		record = handler.getResult();

		assertThat(record, notNullValue());
		assertThat(record.get("integer"), notNullValue());
		assertTrue(record.get("integer") instanceof Integer);
		// Check if the Fpc is adding the timestamp
		assertThat(record.get("timestamp"), notNullValue());
		assertTrue(record.get("timestamp") instanceof ZonedDateTime);
		// Check if the Fpc is adding the requested static attribute
		assertThat(record.get("static"), notNullValue());
		assertTrue(record.get("static") instanceof Integer);
		Integer value = (Integer) record.get("static");
		assertThat(value, equalTo(5));
	}

	@Test
	public void testStaticGet() throws InterruptedException, ExecutionException {
		List<Attribute> attributeList;
		SynchronizerTaskHandler handler;
		Record record;

		attributeList = new ArrayList<>();
		attributeList.add(Attribute.create("static", DataType.INTEGER));
		handler = new SynchronizerTaskHandler();
		fpc.get(attributeList, handler);
		record = handler.getResult();

		assertThat(record, notNullValue());
		// Check if the Fpc is adding the timestamp
		assertThat(record.get("timestamp"), notNullValue());
		assertTrue(record.get("timestamp") instanceof ZonedDateTime);
		// Check if the Fpc is adding the requested static attribute
		assertThat(record.get("static"), notNullValue());
		assertTrue(record.get("static") instanceof Integer);
		Integer value = (Integer) record.get("static");
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
		record = handler1.getLastRecord();
		assertThat(record, notNullValue());
		assertThat(record.get("string"), notNullValue());
		assertTrue(record.get("string") instanceof String);
		assertThat(record.get("integer"), notNullValue());
		assertTrue(record.get("integer") instanceof Integer);
		// Check if the Fpc is adding the timestamp
		assertThat(record.get("timestamp"), notNullValue());
		assertTrue(record.get("timestamp") instanceof ZonedDateTime);

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
		record = handler1.getLastRecord();
		assertThat(record, notNullValue());
		assertThat(record.get("string"), notNullValue());
		assertTrue(record.get("string") instanceof String);
		assertThat(record.get("float"), notNullValue());
		assertTrue(record.get("float") instanceof Float);
		// Check if the Fpc is adding the timestamp
		assertThat(record.get("timestamp"), notNullValue());
		assertTrue(record.get("timestamp") instanceof ZonedDateTime);

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
		record = handler1.getLastRecord();
		assertThat(record, notNullValue());
		assertThat(record.get("string"), notNullValue());
		assertTrue(record.get("string") instanceof String);
		assertThat(record.get("integer"), notNullValue());
		assertTrue(record.get("integer") instanceof Integer);
		// Check if the Fpc is adding the timestamp
		assertThat(record.get("timestamp"), notNullValue());
		assertTrue(record.get("timestamp") instanceof ZonedDateTime);
		// Check if the Fpc is adding the requested static attribute
		assertThat(record.get("static"), notNullValue());
		assertTrue(record.get("static") instanceof Integer);
		Integer value = (Integer) record.get("static");
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
		record = handler.getLastRecord();
		assertThat(record, notNullValue());
		// Check if the Fpc is adding the timestamp
		assertThat(record.get("timestamp"), notNullValue());
		assertTrue(record.get("timestamp") instanceof ZonedDateTime);
		// Check if the Fpc is adding the requested static attribute
		assertThat(record.get("static"), notNullValue());
		assertTrue(record.get("static") instanceof Integer);
		Integer value = (Integer) record.get("static");
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
		record = handler1.getLastRecord();
		assertThat(record, notNullValue());
		assertThat(record.get("boolean"), notNullValue());
		assertTrue(record.get("boolean") instanceof Boolean);
		assertThat(record.get("integer"), notNullValue());
		assertTrue(record.get("integer") instanceof Integer);
		// Check if the Fpc is adding the timestamp
		assertThat(record.get("timestamp"), notNullValue());
		assertTrue(record.get("timestamp") instanceof ZonedDateTime);
	}

	@Test
	public void testSetOperation() throws InterruptedException,
			ExecutionException {
		Map<Attribute, Object> valueMap = new HashMap<>();
		valueMap.put(Attribute.create("integer", DataType.INTEGER), 8);
		SynchronizerTaskHandler handler = new SynchronizerTaskHandler();
		Task task = fpc.set(valueMap, handler);
		assertThat(task, notNullValue());

		Record result = handler.getResult();
		assertThat(result, nullValue());
	}

	@Test
	public void testPeriodicAsyncOperation() throws InterruptedException,
			ExecutionException {
		List<Attribute> attributeList;
		Record record;

		// Request the async event
		attributeList = new ArrayList<>();
		attributeList.add(Attribute.create("event", DataType.BOOLEAN));
		LatchingTaskHandler handler = new LatchingTaskHandler(2);
		Task task = fpc.get(attributeList, 500, handler);

		assertThat(task, notNullValue());
		assertTrue(task instanceof PeriodicTask);
		record = handler.getLastRecord();
		assertThat(record, notNullValue());
		assertThat(record.get("event"), notNullValue());
		assertTrue(record.get("event") instanceof Boolean);
		// Check if the Fpc is adding the timestamp
		assertThat(record.get("timestamp"), notNullValue());
		assertTrue(record.get("timestamp") instanceof ZonedDateTime);
	}

	@Test
	public void testOneoffAsyncOperation() throws InterruptedException,
			ExecutionException {
		List<Attribute> attributeList;
		Record record;

		// Request the async event
		attributeList = new ArrayList<>();
		attributeList.add(Attribute.create("event", DataType.BOOLEAN));
		SynchronizerTaskHandler handler = new SynchronizerTaskHandler();
		Task task = fpc.get(attributeList, handler);

		assertThat(task, notNullValue());
		assertFalse(task instanceof PeriodicTask);
		record = handler.getResult();
		assertThat(record, notNullValue());
	}

	@Test
	public void testAsyncOperation() throws InterruptedException,
			ExecutionException {
		List<Attribute> attributeList;
		Record record;

		// Request the async event
		attributeList = new ArrayList<>();
		attributeList.add(Attribute.create("event", DataType.BOOLEAN));
		LatchingTaskHandler handler = new LatchingTaskHandler(5);
		Task task = fpc.async(attributeList, handler);

		assertThat(task, notNullValue());
		record = handler.getLastRecord();
		assertThat(record, notNullValue());
		assertThat(record.get("event"), notNullValue());
		assertTrue(record.get("event") instanceof Boolean);
		// Check if the Fpc is adding the timestamp
		assertThat(record.get("timestamp"), notNullValue());
		assertTrue(record.get("timestamp") instanceof ZonedDateTime);
	}

}
