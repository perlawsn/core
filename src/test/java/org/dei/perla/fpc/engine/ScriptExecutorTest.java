package org.dei.perla.fpc.engine;

import org.dei.perla.channel.loopback.TestMapper;
import org.dei.perla.fpc.descriptor.AttributeDescriptor;
import org.dei.perla.fpc.descriptor.AttributeDescriptor.AttributePermission;
import org.dei.perla.fpc.descriptor.DataType;
import org.dei.perla.fpc.engine.ExecutionContext.InstructionLocal;
import org.dei.perla.message.Mapper;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class ScriptExecutorTest {

	private static AttributeDescriptor integer;
	private static AttributeDescriptor string;

	private static Mapper mapper1;

	private static Script emitScript;

	@BeforeClass
	public static void setup() {
		integer = new AttributeDescriptor("integer", DataType.INTEGER,
				AttributePermission.READ_WRITE);
		string = new AttributeDescriptor("string", DataType.STRING,
				AttributePermission.READ_WRITE);

		mapper1 = new TestMapper("message1");
		emitScript = ScriptBuilder.newScript()
				.add(new CreateComplexVarInstruction("var", mapper1))
				.add(new SetComplexInstruction("var", "integer", Integer.class, "4"))
				.add(new SetComplexInstruction("var", "string", String.class, "test"))
				.add(new PutInstruction("${var.integer}", integer))
				.add(new PutInstruction("${var.string}", string))
				.add(new EmitInstruction()).add(new StopInstruction())
				.buildScript("testPutEmitInstructions");
	}

	@Test
	public void testSynchronousCallback() throws InterruptedException,
			ExecutionException, ScriptException {
		SynchronizerScriptHandler syncHandler = new SynchronizerScriptHandler();
		Runner runner = Executor.execute(emitScript, syncHandler);
		List<Record> result = syncHandler.getResult();

		assertTrue(runner.isDone());
		assertFalse(runner.isCancelled());
		assertThat(result, notNullValue());
	}

	@Test
	public void testAsynchronousCallback() throws InterruptedException {
		final LinkedBlockingQueue<List<Record>> resultQueue = new LinkedBlockingQueue<>();

		Runner runner = Executor.execute(emitScript, new ScriptHandler() {
			@Override
			public void complete(List<Record> result) {
				resultQueue.add(result);
			}

			@Override
			public void error(Throwable cause) {
				resultQueue.add(null);
			}
		});

		List<Record> result = resultQueue.take();

		assertTrue(runner.isDone());
		assertFalse(runner.isCancelled());
		assertThat(result, notNullValue());
	}

	@Test
	public void testScriptCancellation() throws InterruptedException,
			ScriptException {
		PauseInstruction pauseInstruction = new PauseInstruction();
		Script pauseScript = ScriptBuilder.newScript()
				.add(new CreateComplexVarInstruction("var", mapper1))
				.add(new SetComplexInstruction("var", "integer", Integer.class, "4"))
				.add(new SetComplexInstruction("var", "string", String.class, "test"))
				.add(new PutInstruction("${var.integer}", integer))
				.add(new PutInstruction("${var.string}", string))
				.add(new EmitInstruction()).add(pauseInstruction)
				.add(new StopInstruction())
				.buildScript("testScriptCancellation");

		SynchronizerScriptHandler syncHandler = new SynchronizerScriptHandler();
		Runner runner = Executor.execute(pauseScript, syncHandler);
		assertFalse(runner.isDone());
		runner.cancel();
		pauseInstruction.resume();

		assertTrue(runner.isDone());
		assertTrue(runner.isCancelled());

		try {
			syncHandler.getResult();
		} catch (ExecutionException e) {
			assertThat(e, notNullValue());
			assertTrue(e.getCause() instanceof ScriptCancelledException);
		}
	}

	@Test
	public void testParameterPassing() throws InterruptedException,
			ScriptException, ExecutionException {
		Script script = ScriptBuilder
				.newScript()
				.add(new CreateComplexVarInstruction("var", mapper1))
				.add(new SetComplexInstruction("var", "integer", Integer.class,
						"${param['intParam']}"))
				.add(new SetComplexInstruction("var", "string", String.class,
						"${param['stringParam']}"))
				.add(new PutInstruction("${var.integer}", integer))
				.add(new PutInstruction("${var.string}", string))
				.add(new EmitInstruction()).add(new StopInstruction())
				.buildScript("testParameterPassing");
		ScriptParameter[] paramArray = new ScriptParameter[2];
		paramArray[0] = new ScriptParameter("intParam", 5);
		paramArray[1] = new ScriptParameter("stringParam", "test");

		SynchronizerScriptHandler syncHandler = new SynchronizerScriptHandler();
		Executor.execute(script, paramArray, syncHandler);

		List<Record> result = syncHandler.getResult();
		assertThat(result, notNullValue());
		assertThat(result.size(), equalTo(1));
		Record record = result.get(0);
		assertThat((Integer) record.get("integer"), equalTo(5));
		assertThat((String) record.get("string"), equalTo("test"));
	}

	@Test
	public void testSuspension() throws InterruptedException, ScriptException,
			ExecutionException {
		SuspendInstruction suspendInstruction = new SuspendInstruction();
		Script suspendScript = ScriptBuilder.newScript()
				.add(new CreateComplexVarInstruction("var", mapper1))
				.add(new SetComplexInstruction("var", "integer", Integer.class, "4"))
				.add(new SetComplexInstruction("var", "string", String.class, "test"))
				.add(new PutInstruction("${var.integer}", integer))
				.add(new PutInstruction("${var.string}", string))
				.add(new EmitInstruction()).add(suspendInstruction)
				.add(new StopInstruction()).buildScript("testSuspension");

		SynchronizerScriptHandler syncHandler = new SynchronizerScriptHandler();
		Runner runner = Executor.execute(suspendScript, syncHandler);

		suspendInstruction.waitSuspend();
		assertFalse(runner.isDone());
		assertFalse(runner.isCancelled());

		Executor.resume(runner);
		List<Record> result = syncHandler.getResult();
		assertTrue(runner.isDone());
		assertFalse(runner.isCancelled());
		assertFalse(result.isEmpty());
	}

	@Test
	public void testInstructionLocalVariable() {
		Runner runner1 = new Runner(emitScript, Executor.EMPTY_PARAMETER_ARRAY,
				null, null);
		Runner runner2 = new Runner(emitScript, Executor.EMPTY_PARAMETER_ARRAY,
				null, null);

		InstructionLocal<Integer> il = new InstructionLocal<>(5);

		assertThat(il.getValue(runner1), equalTo(il.getValue(runner2)));

		il.setValue(runner1, 12);
		il.setValue(runner2, 0);

		assertThat(il.getValue(runner1), not(equalTo(il.getValue(runner2))));
		assertThat(il.getValue(runner1), equalTo(12));
		assertThat(il.getValue(runner2), equalTo(0));
	}

}
