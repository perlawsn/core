package org.dei.perla.core.engine;

import org.dei.perla.core.channel.Channel;
import org.dei.perla.core.channel.IORequestBuilder;
import org.dei.perla.core.channel.loopback.LoopbackChannel;
import org.dei.perla.core.channel.loopback.LoopbackIORequestBuilder;
import org.dei.perla.core.channel.loopback.TestMapper;
import org.dei.perla.core.engine.ExecutionContext.InstructionLocal;
import org.dei.perla.core.engine.SubmitInstruction.RequestParameter;
import org.dei.perla.core.message.Mapper;
import org.junit.BeforeClass;
import org.junit.Test;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class ScriptExecutorTest {

	private static Mapper mapper1;

	private static Script emitScript;

	@BeforeClass
	public static void setup() {

		mapper1 = new TestMapper("message1");
		emitScript = ScriptBuilder.newScript()
				.add(new CreateComplexVarInstruction("var", mapper1))
				.add(new SetComplexInstruction("var", "integer", Integer.class, "4"))
				.add(new SetComplexInstruction("var", "string", String.class, "test"))
				.add(new PutInstruction("${var.integer}", Integer.class, 0))
				.add(new PutInstruction("${var.string}", String.class, 1))
				.add(new EmitInstruction()).add(new StopInstruction())
				.buildScript("testPutEmitInstructions");
	}

	@Test
	public void testSynchronousCallback() throws InterruptedException,
			ExecutionException, ScriptException {
		SynchronizerScriptHandler syncHandler = new SynchronizerScriptHandler();
		Runner runner = Executor.execute(emitScript, syncHandler);
		List<Object[]> result = syncHandler.getResult();

		assertTrue(runner.isDone());
		assertFalse(runner.isCancelled());
		assertThat(result, notNullValue());
	}

	@Test
	public void testAsynchronousCallback() throws InterruptedException {
		final LinkedBlockingQueue<List<Object[]>> resultQueue = new
				LinkedBlockingQueue<>();

		Runner runner = Executor.execute(emitScript, new ScriptHandler() {
			@Override
			public void complete(Script script, List<Object[]> samples) {
				assertThat(script, equalTo(emitScript));
				resultQueue.add(samples);
			}

			@Override
			public void error(Script script, Throwable cause) {
				resultQueue.add(null);
			}
		});

		List<Object[]> result = resultQueue.take();

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
				.add(new PutInstruction("${var.integer}", Integer.class, 0))
				.add(new PutInstruction("${var.string}", String.class, 1))
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
				.add(new PutInstruction("${var.integer}", Integer.class, 0))
				.add(new PutInstruction("${var.string}", String.class, 1))
				.add(new EmitInstruction()).add(new StopInstruction())
				.buildScript("testParameterPassing");
		ScriptParameter[] paramArray = new ScriptParameter[2];
		paramArray[0] = new ScriptParameter("intParam", 5);
		paramArray[1] = new ScriptParameter("stringParam", "test");

		SynchronizerScriptHandler syncHandler = new SynchronizerScriptHandler();
		Executor.execute(script, paramArray, syncHandler);

		List<Object[]> result = syncHandler.getResult();
		assertThat(result, notNullValue());
		assertThat(result.size(), equalTo(1));
		Object[] sample = result.get(0);
		assertThat((Integer) sample[0], equalTo(5));
		assertThat((String) sample[1], equalTo("test"));
	}

    @Test
    public void testAttributeOrder() throws InterruptedException,
            ScriptException, ExecutionException {
        Script script = ScriptBuilder
                .newScript()
                .add(new CreateComplexVarInstruction("var", mapper1))
                .add(new SetComplexInstruction("var", "integer", Integer.class, "12"))
                .add(new SetComplexInstruction("var", "string", String.class, "test_order"))
                .add(new PutInstruction("${var.integer}", Integer.class, 0))
                .add(new PutInstruction("${var.integer}", Integer.class, 0))
                .add(new PutInstruction("${var.string}", String.class, 1))
                .add(new EmitInstruction())
                .add(new StopInstruction())
                .buildScript("testOrder");

        SynchronizerScriptHandler handler = new SynchronizerScriptHandler();
        Runner runner = Executor.execute(script, handler);

        List<Object[]> res = handler.getResult();
        assertTrue(runner.isDone());
        assertThat(res.size(), equalTo(1));

        Object[] s = res.get(0);
        assertThat(s.length, equalTo(2));
		assertTrue(s[0] instanceof Integer);
		assertThat(s[0], equalTo(12));
		assertTrue(s[1] instanceof String);
        assertThat(s[1], equalTo("test_order"));
    }

	@Test
	public void testSuspension() throws InterruptedException, ScriptException,
			ExecutionException {
		SuspendInstruction suspendInstruction = new SuspendInstruction();
		Script suspendScript = ScriptBuilder.newScript()
				.add(new CreateComplexVarInstruction("var", mapper1))
				.add(new SetComplexInstruction("var", "integer", Integer.class, "4"))
				.add(new SetComplexInstruction("var", "string", String.class, "test"))
				.add(new PutInstruction("${var.integer}", Integer.class, 0))
				.add(new PutInstruction("${var.string}", String.class, 1))
				.add(new EmitInstruction()).add(suspendInstruction)
				.add(new StopInstruction()).buildScript("testSuspension");

		SynchronizerScriptHandler syncHandler = new SynchronizerScriptHandler();
		Runner runner = Executor.execute(suspendScript, syncHandler);

		suspendInstruction.waitSuspend();
		assertFalse(runner.isDone());
		assertFalse(runner.isCancelled());

		Executor.resume(runner);
		List<Object[]> samples = syncHandler.getResult();
		assertTrue(runner.isDone());
		assertFalse(runner.isCancelled());
		assertFalse(samples.isEmpty());
	}

	@Test
	public void testScriptTimestampCreation() throws Exception {
		Script script = ScriptBuilder.newScript()
				.add(new PutInstruction("${now()}", Instant.class, 0))
				.add(new EmitInstruction())
				.add(new StopInstruction())
				.buildScript("timestamp");

		SynchronizerScriptHandler h = new SynchronizerScriptHandler();
		Executor.execute(script, h);
		List<Object[]> res = h.getResult();
		assertThat(res, notNullValue());
		assertThat(res.size(), equalTo(1));
		assertTrue(res.get(0)[0] instanceof Instant);
		assertThat((Instant) res.get(0)[0], lessThanOrEqualTo(Instant.now()));
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

	@Test
	public void testHandlerError() throws InterruptedException {
		Script s = ScriptBuilder.newScript()
				.add(new StopInstruction())
				.buildScript("test");
		ErrorScriptHandler handler = new ErrorScriptHandler();
		Executor.execute(s, handler);
		Throwable e = handler.awaitError();
		assertTrue(e instanceof ScriptException);
	}

	@Test
	public void stressTest() throws InterruptedException {
		int count = 1000;

		Channel channel = new LoopbackChannel();
		IORequestBuilder request = new LoopbackIORequestBuilder("request");
		Instruction submit = new SubmitInstruction(request, channel,
				new RequestParameter[] { new RequestParameter("var", "var",
						mapper1) }, "output", mapper1);
		Script script = ScriptBuilder
				.newScript()
				.add(new CreateComplexVarInstruction("var", mapper1))
				.add(new SetComplexInstruction("var", "integer", Integer.class, "12"))
				.add(new SetComplexInstruction("var", "string", String.class, "test_order"))
				.add(submit)
				.add(new PutInstruction("${output.integer}", Integer.class, 0))
				.add(new PutInstruction("${output.integer}", Integer.class, 0))
				.add(new PutInstruction("${output.string}", String.class, 1))
				.add(new EmitInstruction())
				.add(new StopInstruction())
				.buildScript("stress_test");

		LatchingScriptHandler h = new LatchingScriptHandler(count);
		for (int i = 0; i < count; i++) {
			Executor.execute(script, h);
		}

		h.await();
	}

}
