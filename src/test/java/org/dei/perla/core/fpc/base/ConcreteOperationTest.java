package org.dei.perla.core.fpc.base;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

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
import org.dei.perla.core.fpc.Task;
import org.dei.perla.core.fpc.base.AsyncOperation.AsyncMessageHandler;
import org.dei.perla.core.fpc.base.NativePeriodicOperation.PeriodicMessageHandler;
import org.dei.perla.core.fpc.descriptor.AttributeDescriptor;
import org.dei.perla.core.fpc.descriptor.ChannelDescriptor;
import org.dei.perla.core.fpc.descriptor.DeviceDescriptor;
import org.dei.perla.core.fpc.descriptor.IORequestDescriptor;
import org.dei.perla.core.fpc.descriptor.MessageDescriptor;
import org.dei.perla.core.fpc.engine.CreateComplexVarInstruction;
import org.dei.perla.core.fpc.engine.EmitInstruction;
import org.dei.perla.core.fpc.engine.PutInstruction;
import org.dei.perla.core.fpc.engine.Record;
import org.dei.perla.core.fpc.engine.Script;
import org.dei.perla.core.fpc.engine.ScriptBuilder;
import org.dei.perla.core.fpc.engine.SetComplexInstruction;
import org.dei.perla.core.fpc.engine.StopInstruction;
import org.dei.perla.core.fpc.engine.SubmitInstruction;
import org.dei.perla.core.fpc.engine.SubmitInstruction.RequestParameter;
import org.dei.perla.core.message.Mapper;
import org.dei.perla.core.message.MapperFactory;
import org.junit.BeforeClass;
import org.junit.Test;

public class ConcreteOperationTest {

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
		JAXBContext jc = JAXBContext.newInstance("org.dei.perla.core.fpc.descriptor"
				+ ":org.dei.perla.core.fpc.descriptor.instructions"
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
				.add(new PutInstruction("${result.integer}", attributeMap.get("integer")))
				.add(new PutInstruction("${result.float}", attributeMap.get("float")))
				.add(new PutInstruction("${result.string}", attributeMap.get("string")))
				.add(new EmitInstruction()).buildScript("on_script");
		List<PeriodicMessageHandler> perHandlerList = new ArrayList<>();
		perHandlerList.add(new PeriodicMessageHandler(true, mmMap
				.get("all-msg"), "result", perOnScript));

		natPeriodicOp = new NativePeriodicOperation("periodic_operation",
				Collections.emptySet(), perStartScript, perStopScript,
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
				.add(new PutInstruction("${result.event}", attributeMap.get("event")))
				.add(new EmitInstruction()).buildScript("on_script");
		AsyncMessageHandler handler = new AsyncMessageHandler(
				mmMap.get("event-msg"), asyncOnScript, "result");

		asyncOp = new AsyncOperation("async_operation", Collections.emptySet(),
				asyncStartScript, handler, chMgr);

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
				.add(new PutInstruction("${res.integer}", attributeMap.get("integer")))
				.add(new PutInstruction("${res.float}", attributeMap.get("float")))
				.add(new PutInstruction("${res.boolean}", attributeMap.get("boolean")))
				.add(new PutInstruction("${res.string}", attributeMap.get("string")))
				.add(new EmitInstruction()).add(new StopInstruction())
				.buildScript("test");
		getOp = new OneoffOperation("test", Collections.emptySet(), getScript);
		simPeriodicOp = new SimulatedPeriodicOperation("test_sim",
				Collections.emptySet(), getScript);

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

		setOp = new OneoffOperation("test", Collections.emptySet(), script);
	}

	@Test
	public void testGetOperation() throws Exception {
		SynchronizerTaskHandler syncHandler = new SynchronizerTaskHandler();
		Task task = getOp.schedule(null, syncHandler, RecordPipeline.EMPTY);
		assertThat(task, notNullValue());
		assertTrue(getOp.getAttributes().containsAll(task.getAttributes()));
		assertTrue(task.getAttributes().containsAll(getOp.getAttributes()));

		Record record = syncHandler.getResult();
		assertFalse(task.isRunning());
		assertThat(record, notNullValue());

		Object value = record.get("integer");
		assertThat(value, notNullValue());
		assertTrue(value instanceof Integer);
		assertThat((Integer) value, equalTo(5));

		value = record.get("float");
		assertThat(value, notNullValue());
		assertTrue(value instanceof Float);
		assertThat((Float) value, equalTo(5.2f));

		value = record.get("boolean");
		assertThat(value, notNullValue());
		assertTrue(value instanceof Boolean);
		assertThat((Boolean) value, equalTo(false));

		value = record.get("string");
		assertThat(value, notNullValue());
		assertTrue(value instanceof String);
		assertThat((String) value, equalTo("test"));
	}

	@Test
	public void testSetOperation() throws Exception {
		SynchronizerTaskHandler syncHandler = new SynchronizerTaskHandler();
		Task task = setOp.schedule(null, syncHandler, RecordPipeline.EMPTY);
		assertThat(task, notNullValue());

		Record record = syncHandler.getResult();
		assertThat(record, nullValue());
		assertFalse(task.isRunning());
	}

	@Test
	public void singleNativePeriodicOperation() throws InterruptedException {
		LatchingTaskHandler handler = new LatchingTaskHandler(1000);
		Map<String, Object> parameterMap = new HashMap<>();
		parameterMap.put("period", 1);

		// Start test
		Task task = natPeriodicOp.schedule(parameterMap, handler, RecordPipeline.EMPTY);
		assertTrue(natPeriodicOp.getAttributes().containsAll(
				task.getAttributes()));
		assertTrue(task.getAttributes().containsAll(
				natPeriodicOp.getAttributes()));
		assertThat(task, notNullValue());
		assertTrue(task.isRunning());
		assertThat(handler.getAveragePeriod(), greaterThanOrEqualTo(0.9d));
		assertThat(handler.getAveragePeriod(), lessThanOrEqualTo(1.1d));

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

		Task task1 = natPeriodicOp.schedule(paramMap1, h1, RecordPipeline.EMPTY);
		assertThat(natPeriodicOp.getSamplingPeriod(), equalTo(100l));
		Task task2 = natPeriodicOp.schedule(paramMap2, h2, RecordPipeline.EMPTY);
		assertThat(natPeriodicOp.getSamplingPeriod(), equalTo(10l));
		Task task3 = natPeriodicOp.schedule(paramMap3, h3, RecordPipeline.EMPTY);
		assertThat(natPeriodicOp.getSamplingPeriod(), equalTo(1l));

		assertThat(task1, notNullValue());
		assertThat(task2, notNullValue());
		assertThat(task3, notNullValue());

		assertTrue(task1.isRunning());
		assertTrue(task2.isRunning());
		assertTrue(task3.isRunning());

		assertThat(h1.getCount(), greaterThanOrEqualTo(10));
		assertThat(h1.getCount(), lessThanOrEqualTo(12));
		assertThat(h1.getAveragePeriod(), greaterThanOrEqualTo(90d));
		assertThat(h1.getAveragePeriod(), lessThanOrEqualTo(120d));

		assertThat(h2.getCount(), greaterThanOrEqualTo(100));
		assertThat(h2.getCount(), lessThanOrEqualTo(110));
		assertThat(h2.getAveragePeriod(), greaterThanOrEqualTo(9d));
		assertThat(h2.getAveragePeriod(), lessThanOrEqualTo(11d));

		assertThat(h3.getCount(), greaterThanOrEqualTo(1000));
		assertThat(h3.getCount(), lessThanOrEqualTo(1010));
		assertThat(h3.getAveragePeriod(), greaterThanOrEqualTo(0.9d));
		assertThat(h3.getAveragePeriod(), lessThanOrEqualTo(1.1d));

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
		LatchingTaskHandler handler = new LatchingTaskHandler(1000);
		Map<String, Object> parameterMap = new HashMap<>();
		parameterMap.put("period", 1);

		// Start test
		Task task = simPeriodicOp.schedule(parameterMap, handler, RecordPipeline.EMPTY);
		assertThat(task, notNullValue());
		assertTrue(task.isRunning());
		assertThat(handler.getAveragePeriod(), greaterThanOrEqualTo(0.9d));
		assertThat(handler.getAveragePeriod(), lessThanOrEqualTo(1.1d));

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

		Task task1 = simPeriodicOp.schedule(paramMap1, h1, RecordPipeline.EMPTY);
		assertThat(simPeriodicOp.getSamplingPeriod(), equalTo(100l));
		Task task2 = simPeriodicOp.schedule(paramMap2, h2, RecordPipeline.EMPTY);
		assertThat(simPeriodicOp.getSamplingPeriod(), equalTo(10l));
		Task task3 = simPeriodicOp.schedule(paramMap3, h3, RecordPipeline.EMPTY);
		assertThat(simPeriodicOp.getSamplingPeriod(), equalTo(1l));

		assertThat(task1, notNullValue());
		assertThat(task2, notNullValue());
		assertThat(task3, notNullValue());

		assertTrue(task1.isRunning());
		assertTrue(task2.isRunning());
		assertTrue(task3.isRunning());

		assertThat(h1.getCount(), greaterThanOrEqualTo(10));
		assertThat(h1.getCount(), lessThanOrEqualTo(12));
		assertThat(h1.getAveragePeriod(), greaterThanOrEqualTo(90d));
		assertThat(h1.getAveragePeriod(), lessThanOrEqualTo(120d));

		assertThat(h2.getCount(), greaterThanOrEqualTo(100));
		assertThat(h2.getCount(), lessThanOrEqualTo(110));
		assertThat(h2.getAveragePeriod(), greaterThanOrEqualTo(9d));
		assertThat(h2.getAveragePeriod(), lessThanOrEqualTo(11d));

		assertThat(h3.getCount(), greaterThanOrEqualTo(1000));
		assertThat(h3.getCount(), lessThanOrEqualTo(1010));
		assertThat(h3.getAveragePeriod(), greaterThanOrEqualTo(0.9d));
		assertThat(h3.getAveragePeriod(), lessThanOrEqualTo(1.1d));

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
		SynchronizerTaskHandler handler = new SynchronizerTaskHandler();

		Task task = asyncOp.getAsyncOneoffOperation().schedule(
				Collections.emptyMap(), handler);
		assertThat(task, notNullValue());
		Record record = handler.getResult();
		assertThat(record, notNullValue());
	}

	@Test
	public void asyncSimulatedPeriodicOperation() throws Exception {
		LatchingTaskHandler handler = new LatchingTaskHandler(5);

		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("period", 100);
		Task task = asyncOp.getAsyncPeriodicOperation()
				.schedule(paramMap, handler);

		assertThat(task, notNullValue());
		assertTrue(task instanceof PeriodicTask);
		Record result = handler.getLastRecord();
		assertThat(result, notNullValue());
		assertThat(result.get("event"), notNullValue());
	}

	@Test
	public void singleAsyncRequest() throws Exception {
		LatchingTaskHandler handler = new LatchingTaskHandler(10);

		Task task = asyncOp.schedule(Collections.emptyMap(), handler);
		assertThat(task, notNullValue());
		Record result = handler.getLastRecord();
		assertThat(result, notNullValue());
		assertThat(result.get("event"), notNullValue());
	}

	@Test
	public void multipleAsyncRequest() throws Exception {
		LatchingTaskHandler handler1 = new LatchingTaskHandler(10);
		LatchingTaskHandler handler2 = new LatchingTaskHandler(10);
		LatchingTaskHandler handler3 = new LatchingTaskHandler(10);

		Task task1 = asyncOp.schedule(Collections.emptyMap(), handler1);
		Task task2 = asyncOp.schedule(Collections.emptyMap(), handler2);
		Task task3 = asyncOp.schedule(Collections.emptyMap(), handler3);

		assertThat(task1, notNullValue());
		assertThat(task2, notNullValue());
		assertThat(task3, notNullValue());

		Record result1 = handler1.getLastRecord();
		assertThat(result1, notNullValue());
		assertThat(result1.get("event"), notNullValue());
		Record result2 = handler2.getLastRecord();
		assertThat(result2, notNullValue());
		assertThat(result2.get("event"), notNullValue());
		Record result3 = handler3.getLastRecord();
		assertThat(result3, notNullValue());
		assertThat(result3.get("event"), notNullValue());
	}

}
