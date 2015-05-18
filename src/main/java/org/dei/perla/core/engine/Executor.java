package org.dei.perla.core.engine;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.el.ValueExpression;

import org.apache.http.annotation.ThreadSafe;
import org.apache.log4j.Logger;
import org.dei.perla.core.utils.Conditions;

/**
 * <p>
 * The <code>Executor</code> class provides various convenience methods for
 * executing <code>Script</code>s and evaluating expressions found in various
 * <code>Script Instruction</code>s.
 * </p>
 *
 * <p>
 * All methods used for executing <code>Script</code>s return a
 * <code>Runner</code> object to allow the callers to control and manage a
 * <code>Script</code> once it has started. Each <code>Script</code> is run in a
 * dedicated thread. It is therefore important that access to shared data
 * structures from within <code>Instruction</code> be properly guarded with
 * adequate concurrency control mechanisms (locks, immutability, etc.).
 * </p>
 *
 * <p>
 * The <code>resume</code> method is provided for restarting execution after
 * suspension. <code>ScriptDebugger</code>s and
 * <code>ScriptHandler<code>s are preserved during suspension.
 * </p>
 *
 *
 * @author Guido Rota (2014)
 *
 */
@ThreadSafe
public class Executor {

	public static final ScriptParameter[] EMPTY_PARAMETER_ARRAY = new ScriptParameter[0];

	private static final Logger logger = Logger.getLogger(Executor.class);

	private static final AtomicBoolean isRunning = new AtomicBoolean(true);
	private static final ExpressionFactory expFct = ExpressionFactory.newInstance();

	private static final ExecutorService pool;
	static {
		// Custom thread naming pattern
		pool = Executors.newCachedThreadPool(new ThreadFactory() {

			private final ThreadFactory fct = Executors.defaultThreadFactory();

			@Override
			public Thread newThread(Runnable r) {
				Thread t = fct.newThread(r);
				t.setName("Executor_" + t.getName());
				return t;
			}

		});
	}

	/**
	 * Shuts down the executor. Execution of this method will prevent new
	 * {@link Script}s to be started, although previously submitted
	 * {@link Script}s will continue to run until they terminate normally or the
	 * the timeout expires, whichever comes first.
	 *
	 * @param timeoutSec
	 *            seconds that this method waits before interrupting all
	 *            running {@link Script}s.
	 */
	public static void shutdown(int timeoutSec) throws InterruptedException {
		if (!isRunning.compareAndSet(true, false)) {
			return;
		}

		pool.shutdown();
		boolean terminated = pool.awaitTermination(timeoutSec, TimeUnit.SECONDS);
		if (!terminated) {
			logger.info("Termination timeout expired, attempting to interrupt lingering Scripts");
			pool.shutdownNow();
		}
	}

	/**
	 * Indicates if the {@code Executor} is currently running and can accept new
	 * {@link Script}s.
	 */
	public static boolean isRunning() {
		return isRunning.get();
	}

	/**
	 * Runs a <code>Script</code> with a <code>ScriptHandler</code> attached.
	 * The <code>ScriptHandler</code> is invoked when the <code>Script</code> is
	 * terminated, normally or abnormally.
	 *
	 * @param script
	 *            <code>Script</code> to execute
	 * @param handler
	 *            <code>ScriptHandler</code> to be invoke upon
	 *            <code>Script</code> termination.
	 * @return <code>Runner</code> object for controlling <code>Script</code>
	 *         execution
	 */
	public static Runner execute(Script script, ScriptHandler handler) {
		return execute(script, EMPTY_PARAMETER_ARRAY, handler, null);
	}

	/**
	 * Runs a <code>Script</code> with a <code>ScriptHandler</code> attached.
	 * The <code>ScriptHandler</code> is invoked when the <code>Script</code> is
	 * terminated, normally or abnormally.
	 *
	 * @param script
	 *            <code>Script</code> to execute
	 * @param paramArray
	 *            Array of parameters to pass to the <code>Script</code>
	 * @param handler
	 *            <code>ScriptHandler</code> to be invoke upon
	 *            <code>Script</code> termination.
	 * @return <code>Runner</code> object for controlling <code>Script</code>
	 *         execution
	 */
	public static Runner execute(Script script, ScriptParameter[] paramArray,
			ScriptHandler handler) {
		return execute(script, paramArray, handler, null);
	}

	/**
	 * Runs a <code>Script</code> with both a <code>ScriptHandler</code> and a
	 * <code>ScriptDebugger</code> attached. The <code>ScriptHandler</code> is
	 * invoked when the <code>Script</code> is terminated, normally or
	 * abnormally.
	 *
	 * @param script
	 *            <code>Script</code> to execute
	 * @param paramArray
	 *            Array of parameters to pass to the <code>Script</code>
	 * @param handler
	 *            <code>ScriptHandler</code> to be invoke upon
	 *            <code>Script</code> termination.
	 * @param debugger
	 *            <code>ScriptDebugger</code> to invoke when the execution hits
	 *            a breakpoint
	 * @return <code>Runner</code> object for controlling <code>Script</code>
	 *         execution
	 */
	public static Runner execute(Script script, ScriptParameter[] paramArray,
			ScriptHandler handler, ScriptDebugger debugger) {

		if (!isRunning.get()) {
			throw new RejectedExecutionException(
					"Cannot start, Executor has been stopped");
		}

		logger.debug("Executing script " + script.getName());

		script = Conditions.checkNotNull(script, "script");
		paramArray = Conditions.checkNotNull(paramArray, "paramArray");
		handler = Conditions.checkNotNull(handler, "handler");

		final Runner runner = new Runner(script, paramArray, handler, debugger);
		pool.submit(runner::execute);
		return runner;
	}

	/**
	 * Resumes a previously suspended <code>Script</code>.
	 *
	 * @param runner
	 *            <code>Runner</code> object representing the suspended
	 *            <code>Script</code>
	 */
	public static void resume(final Runner runner) {
		if (!runner.isSuspended()) {
			throw new IllegalStateException(
					"Cannot resume, runner is not in suspended state");
		}
		pool.submit(runner::execute);
	}

	/**
	 * Convenience method for creating a
	 * <code>ValueExpression<code> that wraps a Java object.
	 *
	 * @param value
	 *            The object to be wrapped
	 * @param type
	 *            Type of the object to be wrapped
	 * @return <code>ValueExpression</code> wrapping a Java object
	 */
	protected static ValueExpression createValueExpression(Object value,
			Class<?> type) {
		return expFct.createValueExpression(value, type);
	}

	/**
	 * Evaluates an EL expression.
	 *
	 * @param context
	 *            <code>ExecutionContext</code> to be used during evaluation
	 * @param expression
	 *            Expression to evaluate
	 * @return Result of the expression
	 */
	protected static Object evaluateExpression(ExecutionContext context,
			String expression) {
		return evaluateExpression(context, expression, Object.class);
	}

	/**
	 * Evaluates an EL expression and returns the results appropriately coerced
	 * into the specified type.
	 *
	 * @param context
	 *            <code>ExecutionContext</code> to be used during evaluation
	 * @param expression
	 *            Expression to evaluate
	 * @param type
	 *            Result type
	 * @return Result of the expression coerced into the specified type
	 */
	// @SuppressWarnings("unchecked") If something goes wrong it's because the
	// DeviceDescriptor from which the type information was taken is wrong.
	@SuppressWarnings("unchecked")
	protected static <T> T evaluateExpression(ExecutionContext context,
			String expression, Class<T> type) {

		ELContext elContext = context.getELContext();

		ValueExpression result = expFct.createValueExpression(elContext,
				expression, type);
		return (T) result.getValue(elContext);
	}

}
