package org.dei.perla.core.engine;

import org.dei.perla.core.channel.Channel;
import org.dei.perla.core.channel.IORequestBuilder;
import org.dei.perla.core.channel.loopback.LoopbackChannel;
import org.dei.perla.core.channel.loopback.LoopbackIORequestBuilder;
import org.dei.perla.core.channel.loopback.TestMapper;
import org.dei.perla.core.channel.loopback.TestMessage;
import org.dei.perla.core.descriptor.AttributeDescriptor;
import org.dei.perla.core.descriptor.AttributeDescriptor.AttributePermission;
import org.dei.perla.core.descriptor.DataType;
import org.dei.perla.core.engine.SubmitInstruction.RequestParameter;
import org.dei.perla.core.message.FpcMessage;
import org.dei.perla.core.message.Mapper;
import org.dei.perla.core.record.Attribute;
import org.dei.perla.core.record.Record;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class ScriptInstructionsTest {

	private static AttributeDescriptor integer;
	private static AttributeDescriptor string;
	private static AttributeDescriptor bool;

	private static Mapper mapper1;
	private static Mapper mapper2;

	private static Channel channel;
	private static IORequestBuilder request1;

	@BeforeClass
	public static void setup() {
		integer = new AttributeDescriptor("integer", DataType.INTEGER,
				AttributePermission.READ_WRITE);
		string = new AttributeDescriptor("string", DataType.STRING,
				AttributePermission.READ_WRITE);
		bool = new AttributeDescriptor("bool", DataType.BOOLEAN,
				AttributePermission.READ_WRITE);

		mapper1 = new TestMapper("message1");
		mapper2 = new TestMapper("message2");

		channel = new LoopbackChannel();
		request1 = new LoopbackIORequestBuilder("request1");
	}

	@Test
	public void testCreateSimpleInstruction() throws Exception {
		final AtomicBoolean inDebugger = new AtomicBoolean(false);
		Script script = ScriptBuilder
				.newScript()
				.add(new CreatePrimitiveVarInstruction("integer", DataType.INTEGER))
				.add(new CreatePrimitiveVarInstruction("string", DataType.STRING))
				.add(new CreatePrimitiveVarInstruction("float", DataType.FLOAT))
				.add(new CreatePrimitiveVarInstruction("boolean", DataType.BOOLEAN))
				.add(new CreatePrimitiveVarInstruction("id", DataType.ID))
				.add(new BreakpointInstruction()).add(new StopInstruction())
				.buildScript("testCreatePrimitiveVarInstruction");

		SynchronizerScriptHandler syncHandler = new SynchronizerScriptHandler();
		Executor.execute(script, new ScriptParameter[] {}, syncHandler,
                (Runner r, Script s, Instruction i) -> {
                    inDebugger.set(true);

                    Object obj = r.ctx.getVariable("integer");
                    assertThat(obj, notNullValue());
                    assertTrue(obj instanceof Integer);

                    obj = r.ctx.getVariable("string");
                    assertThat(obj, notNullValue());
                    assertTrue(obj instanceof String);

                    obj = r.ctx.getVariable("float");
                    assertThat(obj, notNullValue());
                    assertTrue(obj instanceof Float);

                    obj = r.ctx.getVariable("boolean");
                    assertThat(obj, notNullValue());
                    assertTrue(obj instanceof Boolean);

                    obj = r.ctx.getVariable("id");
                    assertThat(obj, notNullValue());
                    assertTrue(obj instanceof Integer);
				});
		// Wait until script execution terminates
		syncHandler.getResult();
		assertTrue(inDebugger.get());
	}

	@Test
	public void testCreateComplexVarInstruction() throws Exception {
		final AtomicBoolean inDebugger = new AtomicBoolean(false);
		Script script = ScriptBuilder.newScript()
				.add(new CreateComplexVarInstruction("var1", mapper1))
				.add(new CreateComplexVarInstruction("var2", mapper2))
				.add(new CreateComplexVarInstruction("var3", mapper2))
				.add(new BreakpointInstruction()).add(new StopInstruction())
				.buildScript("testCreateComplexVarInstruction");

		SynchronizerScriptHandler syncHandler = new SynchronizerScriptHandler();
		Executor.execute(script, new ScriptParameter[] {}, syncHandler,
                (Runner r, Script s, Instruction i) -> {
                    inDebugger.set(true);
                    TestMessage variable = (TestMessage) r.ctx
                            .getVariable("var1");
                    assertThat(variable, notNullValue());
                    assertThat(variable.getMessageId(), equalTo("message1"));

                    variable = (TestMessage) r.ctx.getVariable("var2");
                    assertThat(variable, notNullValue());
                    assertThat(variable.getMessageId(), equalTo("message2"));

                    variable = (TestMessage) r.ctx.getVariable("var3");
                    assertThat(variable, notNullValue());
                    assertThat(variable.getMessageId(), equalTo("message2"));
				});
		// Wait until script execution terminates
		syncHandler.getResult();
		assertTrue(inDebugger.get());
	}

	@Test
	public void testSetInstruction() throws Exception {
		final AtomicBoolean inDebugger = new AtomicBoolean(false);
		Script script = ScriptBuilder
				.newScript()
				.add(new CreatePrimitiveVarInstruction("integer", DataType.INTEGER))
				.add(new SetPrimitiveInstruction("integer", Integer.class, "10"))
				.add(new CreateComplexVarInstruction("var1", mapper1))
				.add(new SetComplexInstruction("var1", "integer",
						Integer.class, "${5 + 4}"))
				.add(new SetComplexInstruction("var1", "string", String.class,
						"test"))
				.add(new SetComplexInstruction("var1", "float", Float.class,
						"${5.2 * 0.4}"))
				.add(new CreateComplexVarInstruction("var2", mapper1))
				.add(new SetComplexInstruction("var2", "integer",
						Integer.class, "${var1.integer}"))
				.add(new BreakpointInstruction()).add(new StopInstruction())
				.buildScript("testSetInstruction");

		SynchronizerScriptHandler syncHandler = new SynchronizerScriptHandler();
		Executor.execute(script, new ScriptParameter[] {}, syncHandler,
                (Runner r, Script s, Instruction i) -> {
                    inDebugger.set(true);
                    Object obj = r.ctx.getVariable("integer");
                    assertThat(obj, notNullValue());
                    assertTrue(obj instanceof Integer);
                    assertThat((Integer) obj, equalTo(10));

                    FpcMessage var1 = (FpcMessage) r.ctx
                            .getVariable("var1");
                    FpcMessage var2 = (FpcMessage) r.ctx
                            .getVariable("var2");
                    Object att;

                    att = var1.getField("integer");
                    assertThat(att, notNullValue());
                    assertTrue(att instanceof Integer);
                    assertThat((Integer) att, equalTo(9));

                    att = var1.getField("string");
                    assertThat(att, notNullValue());
                    assertTrue(att instanceof String);
                    assertThat((String) att, equalTo("test"));

                    att = var1.getField("float");
                    assertThat(att, notNullValue());
                    assertTrue(att instanceof Float);
                    assertThat((Float) att, equalTo(2.08f));

                    att = var2.getField("integer");
                    assertThat(att, notNullValue());
                    assertTrue(att instanceof Integer);
                    assertThat((Integer) att, equalTo(9));
				});
		// Wait until script execution terminates
		syncHandler.getResult();
		assertTrue(inDebugger.get());
	}

	@Test
	public void testAppendInstruction() throws Exception {
		final AtomicBoolean inDebugger = new AtomicBoolean(false);
		Script script = ScriptBuilder.newScript()
				.add(new CreateComplexVarInstruction("var", mapper1))
				.add(new AppendInstruction("var", "list", Integer.class, "1"))
				.add(new AppendInstruction("var", "list", Integer.class, "2"))
				.add(new AppendInstruction("var", "list", Integer.class, "3"))
				.add(new BreakpointInstruction())
				.buildScript("testAppendInstruction");
		SynchronizerScriptHandler syncHandler = new SynchronizerScriptHandler();
		Executor.execute(script, new ScriptParameter[] {}, syncHandler,
                (Runner r, Script s, Instruction i) -> {
                    inDebugger.set(true);
                    Object var = r.ctx.getVariable("var");
                    List<?> list = (List<?>) ((FpcMessage) var)
                            .getField("list");

                    assertThat(list, notNullValue());
                    assertThat(list.size(), equalTo(3));
                    assertThat(list.get(0), equalTo(1));
                    assertThat(list.get(1), equalTo(2));
                    assertThat(list.get(2), equalTo(3));
				});
		// Wait until script execution terminates
		syncHandler.getResult();
		assertTrue(inDebugger.get());
	}

	@Test
	public void testForeachInstruction0() throws Exception {
        Instruction body = new PutInstruction("${element}", integer, 0);
        body.setNext(new EmitInstruction());
        List<Attribute> emit = new ArrayList<>();
        emit.add(Attribute.create(integer));
		Script script = ScriptBuilder.newScript()
				.add(new CreateComplexVarInstruction("var", mapper1))
				.add(new AppendInstruction("var", "list", Integer.class, "1"))
				.add(new AppendInstruction("var", "list", Integer.class, "2"))
				.add(new AppendInstruction("var", "list", Integer.class, "3"))
				.add(new ForeachInstruction("var", "list", "element", body))
                .extraEmit(emit)
				.buildScript("testForeachInstruction0");
		SynchronizerScriptHandler syncHandler = new SynchronizerScriptHandler();

		Executor.execute(script, syncHandler);
		List<Record> recordList = syncHandler.getResult();
		assertThat(recordList, notNullValue());
		assertThat(recordList.size(), equalTo(3));

		assertThat(recordList.get(0).getValue("integer"), equalTo(1));
		assertThat(recordList.get(1).getValue("integer"), equalTo(2));
		assertThat(recordList.get(2).getValue("integer"), equalTo(3));
	}

	@Test
	public void testForeachInstruction1() throws Exception {
        Instruction body = new PutInstruction("${element * index}", integer, 0);
        body.setNext(new EmitInstruction());
        List<Attribute> emit = new ArrayList<>();
        emit.add(Attribute.create(integer));
		Script script = ScriptBuilder.newScript()
				.add(new CreateComplexVarInstruction("var", mapper1))
				.add(new AppendInstruction("var", "list", Integer.class, "1"))
				.add(new AppendInstruction("var", "list", Integer.class, "2"))
				.add(new AppendInstruction("var", "list", Integer.class, "3"))
				.add(new ForeachInstruction("var", "list", "element",
                        "index", body))
                .extraEmit(emit)
				.buildScript("testForeachInstruction0");
		SynchronizerScriptHandler syncHandler = new SynchronizerScriptHandler();

		Executor.execute(script, syncHandler);
		List<Record> recordList = syncHandler.getResult();
		assertThat(recordList, notNullValue());
		assertThat(recordList.size(), equalTo(3));

		assertThat(recordList.get(0).getValue("integer"), equalTo(0));
		assertThat(recordList.get(1).getValue("integer"), equalTo(2));
		assertThat(recordList.get(2).getValue("integer"), equalTo(6));
	}

	@Test
	public void testIfInstruction0() throws Exception {
		final AtomicBoolean inDebugger = new AtomicBoolean(false);
		Script script = ScriptBuilder
				.newScript()
				.add(new CreateComplexVarInstruction("var", mapper1))
				.add(new SetComplexInstruction("var", "integer", Integer.class,
						"2"))
				.add(new SetComplexInstruction("var", "boolean", Boolean.class,
						"false"))
				.add(new SetComplexInstruction("var", "string", String.class,
						"before"))
				.add(new BreakpointInstruction())
				.add(new IfInstruction("${var.integer < 5}", ScriptBuilder
						.newScript()
						.add(new SetComplexInstruction("var", "boolean",
								Boolean.class, "true"))
						.add(new SetComplexInstruction("var", "string",
								String.class, "then")).getCode(), ScriptBuilder
						.newScript()
						.add(new SetComplexInstruction("var", "boolean",
								Boolean.class, "true"))
						.add(new SetComplexInstruction("var", "string",
								String.class, "else")).getCode()))
				.add(new BreakpointInstruction()).add(new StopInstruction())
				.buildScript("testIfInstruction0");

		SynchronizerScriptHandler syncHandler = new SynchronizerScriptHandler();
		Executor.execute(script, new ScriptParameter[] {}, syncHandler,
                (Runner r, Script s, Instruction i) -> {
                    inDebugger.set(true);
                    FpcMessage msg = (FpcMessage) r.ctx
                            .getVariable("var");

                    boolean ifExecuted = (boolean) msg.getField("boolean");
                    if (ifExecuted == false) {
                        assertThat((String) msg.getField("string"),
                                equalTo("before"));
                    } else {
                        assertThat((String) msg.getField("string"),
                                equalTo("then"));
                    }
				});
		// Wait until script execution terminates
		syncHandler.getResult();
		assertTrue(inDebugger.get());
	}

	@Test
	public void testIfInstruction1() throws Exception {
		final AtomicBoolean inDebugger = new AtomicBoolean(false);
		Script script = ScriptBuilder
				.newScript()
				.add(new CreateComplexVarInstruction("var", mapper1))
				.add(new SetComplexInstruction("var", "integer", Integer.class,
						"2"))
				.add(new SetComplexInstruction("var", "boolean", Boolean.class,
						"false"))
				.add(new SetComplexInstruction("var", "string", String.class,
						"before"))
				.add(new BreakpointInstruction())
				.add(new IfInstruction("${var.integer > 5}", ScriptBuilder
						.newScript()
						.add(new SetComplexInstruction("var", "boolean",
								Boolean.class, "true"))
						.add(new SetComplexInstruction("var", "string",
								String.class, "then")).getCode(), ScriptBuilder
						.newScript()
						.add(new SetComplexInstruction("var", "boolean",
								Boolean.class, "true"))
						.add(new SetComplexInstruction("var", "string",
								String.class, "else")).getCode()))
				.add(new BreakpointInstruction()).add(new StopInstruction())
				.buildScript("testIfInstruction1");

		SynchronizerScriptHandler syncHandler = new SynchronizerScriptHandler();
		Executor.execute(script, new ScriptParameter[] {}, syncHandler,
                (Runner r, Script s, Instruction i) -> {
                    inDebugger.set(true);
                    FpcMessage msg = (FpcMessage) r.ctx
                            .getVariable("var");

                    boolean ifExecuted = (boolean) msg.getField("boolean");
                    if (ifExecuted == false) {
                        assertThat((String) msg.getField("string"),
                                equalTo("before"));
                    } else {
                        assertThat((String) msg.getField("string"),
                                equalTo("else"));
                    }
				});
		// Wait until script execution terminates
		syncHandler.getResult();
		assertTrue(inDebugger.get());
	}

	@Test
	public void testIfInstruction2() throws Exception {
		final AtomicBoolean inDebugger = new AtomicBoolean(false);
		Script script = ScriptBuilder
				.newScript()
				.add(new CreateComplexVarInstruction("var", mapper1))
				.add(new SetComplexInstruction("var", "integer", Integer.class,
						"8"))
				.add(new SetComplexInstruction("var", "boolean", Boolean.class,
						"false"))
				.add(new SetComplexInstruction("var", "string", String.class,
						"before"))
				.add(new BreakpointInstruction())
				.add(new IfInstruction("${var.integer > 5}", ScriptBuilder
						.newScript()
						.add(new SetComplexInstruction("var", "boolean",
								Boolean.class, "true"))
						.add(new SetComplexInstruction("var", "string",
								String.class, "then")).getCode()))
				.add(new BreakpointInstruction()).add(new StopInstruction())
				.buildScript("testIfInstruction2");

		SynchronizerScriptHandler syncHandler = new SynchronizerScriptHandler();
		Executor.execute(script, new ScriptParameter[] {}, syncHandler,
                (Runner r, Script s, Instruction i) -> {
                    inDebugger.set(true);
                    FpcMessage msg = (FpcMessage) r.ctx
                            .getVariable("var");

                    boolean ifExecuted = (boolean) msg.getField("boolean");
                    if (ifExecuted == false) {
                        assertThat((String) msg.getField("string"),
                                equalTo("before"));
                    } else {
                        assertThat((String) msg.getField("string"),
                                equalTo("then"));
                    }
				});
		// Wait until script execution terminates
		syncHandler.getResult();
		assertTrue(inDebugger.get());
	}

	@Test
	public void testIfInstruction3() throws Exception {
		final AtomicBoolean inDebugger = new AtomicBoolean(false);
		Script script = ScriptBuilder
				.newScript()
				.add(new CreateComplexVarInstruction("var", mapper1))
				.add(new SetComplexInstruction("var", "integer", Integer.class,
						"8"))
				.add(new SetComplexInstruction("var", "boolean", Boolean.class,
						"false"))
				.add(new SetComplexInstruction("var", "string", String.class,
						"before"))
				.add(new BreakpointInstruction())
				.add(new IfInstruction("${var.integer != 8}", ScriptBuilder
						.newScript()
						.add(new SetComplexInstruction("var", "boolean",
								Boolean.class, "true"))
						.add(new SetComplexInstruction("var", "string",
								String.class, "then")).getCode()))
				.add(new BreakpointInstruction()).add(new StopInstruction())
				.buildScript("testIfInstruction3");

		SynchronizerScriptHandler syncHandler = new SynchronizerScriptHandler();
		Executor.execute(script, new ScriptParameter[] {}, syncHandler,
                (Runner r, Script s, Instruction i) -> {
                    inDebugger.set(true);
                    FpcMessage msg = (FpcMessage) r.ctx
                            .getVariable("var");
                    assertThat((String) msg.getField("string"),
                            equalTo("before"));
				});
		// Wait until script execution terminates
		syncHandler.getResult();
		assertTrue(inDebugger.get());
	}

	@Test
	public void testPutEmitInstructions() throws Exception {
		Script script = ScriptBuilder
				.newScript()
				.add(new CreateComplexVarInstruction("var", mapper1))
				.add(new SetComplexInstruction("var", "integer", Integer.class,
						"4"))
				.add(new SetComplexInstruction("var", "string", String.class,
						"test"))
				.add(new SetComplexInstruction("var", "bool", Boolean.class,
						"false"))
				.add(new PutInstruction("${var.integer}", integer, 0))
				.add(new PutInstruction("${var.string}", string, 1))
				.add(new PutInstruction("${!var.bool}", bool, 2))
				.add(new EmitInstruction()).add(new StopInstruction())
				.buildScript("testPutEmitInstructions");

		SynchronizerScriptHandler syncHandler = new SynchronizerScriptHandler();
		Executor.execute(script, syncHandler);
		List<Record> recordList = syncHandler.getResult();
		assertThat(recordList, notNullValue());
		assertThat(recordList.size(), equalTo(1));

		Record r = recordList.get(0);
		for (Attribute a : r.fields()) {
            Object f = r.getValue(a.getId());
			assertThat(f, notNullValue());
			switch (a.getId()) {
			case "integer":
                assertThat(a.getType(), equalTo(DataType.INTEGER));
				assertTrue(f instanceof Integer);
				assertThat(f, equalTo(4));
				break;
			case "string":
                assertThat(a.getType(), equalTo(DataType.STRING));
				assertTrue(f instanceof String);
				assertThat(f, equalTo("test"));
				break;
			case "bool":
                assertThat(a.getType(), equalTo(DataType.BOOLEAN));
				assertTrue(f instanceof Boolean);
				assertThat(f, equalTo(true));
				break;
			default:
				throw new RuntimeException("Unexpected attribute '"
						+ a.getId() + "'.");
			}
		}
	}

	@Test
	public void testComplexTypeManagement() throws Exception {
		Script script = ScriptBuilder
				.newScript()
				.add(new CreateComplexVarInstruction("var1", mapper1))
				.add(new CreateComplexVarInstruction("var2", mapper2))
				.add(new SetComplexInstruction("var1", "integer",
						Integer.class, "4"))
				.add(new SetComplexInstruction("var1", "string", String.class,
						"test"))
				.add(new SetComplexInstruction("var1", "bool", Boolean.class,
						"false"))
				.add(new SetComplexInstruction("var2", "var1",
						FpcMessage.class, "${var1}"))
				.add(new PutInstruction("${var2.var1.integer}", integer, 0))
				.add(new PutInstruction("${var2.var1.string}", string, 1))
				.add(new PutInstruction("${!var2.var1.bool}", bool, 2))
				.add(new EmitInstruction()).add(new StopInstruction())
				.buildScript("testPutEmitInstructions");

		SynchronizerScriptHandler syncHandler = new SynchronizerScriptHandler();
		Executor.execute(script, syncHandler);
		List<Record> recordList = syncHandler.getResult();
		assertThat(recordList, notNullValue());
		assertThat(recordList.size(), equalTo(1));

		Record r = recordList.get(0);
		for (Attribute a : r.fields()) {
            Object f = r.getValue(a.getId());
			assertThat(f, notNullValue());
			switch (a.getId()) {
			case "integer":
                assertThat(a.getType(), equalTo(DataType.INTEGER));
				assertTrue(f instanceof Integer);
				assertThat(f, equalTo(4));
				break;
			case "string":
                assertThat(a.getType(), equalTo(DataType.STRING));
				assertTrue(f instanceof String);
				assertThat(f, equalTo("test"));
				break;
			case "bool":
                assertThat(a.getType(), equalTo(DataType.BOOLEAN));
				assertTrue(f instanceof Boolean);
				assertThat(f, equalTo(true));
				break;
			default:
				throw new RuntimeException("Unexpected attribute '"
						+ a.getId() + "'.");
			}
		}
	}

	@Test
	public void testMultiplePutEmitInstructions() throws Exception {
		Script script = ScriptBuilder
				.newScript()
				.add(new CreateComplexVarInstruction("var", mapper1))
				.add(new SetComplexInstruction("var", "integer", Integer.class,
						"4"))
				.add(new SetComplexInstruction("var", "string", String.class,
						"test"))
				.add(new PutInstruction("${var.integer}", integer, 0))
				.add(new PutInstruction("${var.string}", string, 1))
				.add(new EmitInstruction())
				.add(new SetComplexInstruction("var", "integer", Integer.class,
						"5"))
				.add(new SetComplexInstruction("var", "string", String.class,
						"test"))
				.add(new PutInstruction("${var.integer}", integer, 0))
				.add(new PutInstruction("${var.string}", string, 1))
				.add(new EmitInstruction())
				.add(new SetComplexInstruction("var", "integer", Integer.class,
						"6"))
				.add(new SetComplexInstruction("var", "string", String.class,
						"test"))
				.add(new PutInstruction("${var.integer}", integer, 0))
				.add(new PutInstruction("${var.string}", string, 1))
				.add(new EmitInstruction()).add(new StopInstruction())
				.buildScript("testMultiplePutEmitInstructions");

		SynchronizerScriptHandler syncHandler = new SynchronizerScriptHandler();
		Executor.execute(script, syncHandler);
		List<Record> recordList = syncHandler.getResult();
		assertThat(recordList, notNullValue());
		assertThat(recordList.size(), equalTo(3));

		Record r = recordList.get(0);
		for (Attribute a : r.fields()) {
            Object f = r.getValue(a.getId());
			assertThat(f, notNullValue());
			switch (a.getId()) {
			case "integer":
                assertThat(a.getType(), equalTo(DataType.INTEGER));
				assertTrue(f instanceof Integer);
				assertThat(f, equalTo(4));
				break;
			case "string":
                assertThat(a.getType(), equalTo(DataType.STRING));
				assertTrue(f instanceof String);
				assertThat(f, equalTo("test"));
				break;
			default:
				throw new RuntimeException("Unexpected attribute '"
						+ a.getId() + "'.");
			}
		}

		r = recordList.get(1);
		for (Attribute a : r.fields()) {
            Object f = r.getValue(a.getId());
			assertThat(f, notNullValue());
			switch (a.getId()) {
			case "integer":
                assertThat(a.getType(), equalTo(DataType.INTEGER));
				assertTrue(f instanceof Integer);
				assertThat(f, equalTo(5));
				break;
			case "string":
                assertThat(a.getType(), equalTo(DataType.STRING));
				assertTrue(f instanceof String);
				assertThat(f, equalTo("test"));
				break;
			default:
				throw new RuntimeException("Unexpected attribute '"
						+ a.getId() + "'.");
			}
		}

		r = recordList.get(2);
		for (Attribute a : r.fields()) {
            Object f = r.getValue(a.getId());
			assertThat(f, notNullValue());
			switch (a.getId()) {
			case "integer":
                assertThat(a.getType(), equalTo(DataType.INTEGER));
				assertTrue(f instanceof Integer);
				assertThat(f, equalTo(6));
				break;
			case "string":
                assertThat(a.getType(), equalTo(DataType.STRING));
				assertTrue(f instanceof String);
				assertThat(f, equalTo("test"));
				break;
			default:
				throw new RuntimeException("Unexpected attribute '"
						+ a.getId() + "'.");
			}
		}
	}

	@Test
	public void testErrorInstruction() throws Exception {
		Script script = ScriptBuilder.newScript()
				.add(new ErrorInstruction("Test error instruction"))
				.add(new StopInstruction()).buildScript("testErrorInstruction");

		SynchronizerScriptHandler syncHandler = new SynchronizerScriptHandler();
		Runner runner = Executor.execute(script, syncHandler);
		try {
			syncHandler.getResult();
			assertTrue(runner.isDone());
			assertTrue(runner.isCancelled());
		} catch (ExecutionException e) {
			assertThat(e, notNullValue());
			assertTrue(e.getCause() instanceof ScriptException);
		}
	}

	@Test
	public void testSubmitInstruction() throws Exception {
		Instruction submit = new SubmitInstruction(request1, channel,
				new RequestParameter[] { new RequestParameter("param", "param",
						mapper1) }, "output", mapper2);

		Script script = ScriptBuilder
				.newScript()
				.add(new CreateComplexVarInstruction("param", mapper1))
				.add(new SetComplexInstruction("param", "integer",
						Integer.class, "5"))
				.add(new SetComplexInstruction("param", "string", String.class,
						"test")).add(submit)
				.add(new PutInstruction("${output.integer}", integer, 0))
				.add(new PutInstruction("${output.string}", string, 1))
				.add(new EmitInstruction()).add(new StopInstruction())
				.buildScript("testSubmitInstruction");

		SynchronizerScriptHandler syncHandler = new SynchronizerScriptHandler();
		Executor.execute(script, syncHandler);
		List<Record> recordList = syncHandler.getResult();
		assertThat(recordList, notNullValue());
		assertThat(recordList.size(), equalTo(1));

		Record record = recordList.get(0);
		assertThat(record, notNullValue());
		assertThat(record.getValue("integer"), notNullValue());
		assertThat((Integer) record.getValue("integer"), equalTo(5));
		assertThat(record.getValue("string"), notNullValue());
		assertThat((String) record.getValue("string"), equalTo("test"));
	}

}
