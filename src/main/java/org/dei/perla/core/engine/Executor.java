package org.dei.perla.core.engine;

import org.apache.http.annotation.ThreadSafe;
import org.apache.log4j.Logger;
import org.dei.perla.core.utils.Conditions;

import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.el.ValueExpression;
import java.util.concurrent.*;

/**
 * <p>
 * The {@code Executor} class provides various convenience methods for
 * executing {@link Script}s and evaluating expressions found in various
 * {@link Instruction}s.
 *
 * <p>
 * All methods used for executing {@link Script}s return a
 * {@link Runner} object to allow the callers to control and manage a
 * {@link Script} once it has started. Each {@link Script} is run in a
 * dedicated thread. It is therefore important that access to shared data
 * structures from within {@link Instruction}s be properly guarded with
 * adequate concurrency control mechanisms (locks, immutability, etc.).
 *
 * <p>
 * The {@code resume()} method is provided for restarting execution after
 * suspension. {@link ScriptDebugger} and {@link ScriptHandler} are preserved
 * during suspension.
 *
 *
 * @author Guido Rota (2014)
 *
 */
@ThreadSafe
public class Executor {

    private static final Logger log = Logger.getLogger(Executor.class);

    public static final ScriptParameter[] EMPTY_PARAMETER_ARRAY = new ScriptParameter[0];

    private static boolean running = true;

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
    public static synchronized void shutdown(int timeoutSec)
            throws InterruptedException {
        running = false;
        pool.shutdown();
        boolean terminated = pool.awaitTermination(timeoutSec, TimeUnit.SECONDS);
        if (!terminated) {
            log.info("Termination timeout expired, attempting to interrupt lingering Scripts");
            pool.shutdownNow();
        }
    }

    /**
     * Indicates if the {@code Executor} is currently running and can accept new
     * {@link Script}s.
     */
    public static synchronized boolean isRunning() {
        return running;
    }

    /**
     * Runs a {@link Script} with a {@link ScriptHandler} attached.
     * The {@link ScriptHandler} is invoked when the {@link Script} is
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
     * Runs a {@link Script} with a {@link ScriptHandler} attached.
     * The {@link ScriptHandler} is invoked when the {@link Script} is
     * terminated, normally or abnormally.
     *
     * @param script
     *            {@link Script} to execute
     * @param paramArray
     *            Array of parameters to pass to the {@link Script}
     * @param handler
     *            {@link ScriptHandler} to be invoke upon {@link Script}
     *            termination.
     * @return {@link Runner} object for controlling {@link Script}
     *         execution
     */
    public static Runner execute(Script script, ScriptParameter[] paramArray,
            ScriptHandler handler) {
        return execute(script, paramArray, handler, null);
    }

    /**
     * Runs a {@link Script} with both a {@link ScriptHandler} and a
     * {@link ScriptDebugger} attached. The {@link ScriptHandler} is
     * invoked when the {@link Script} is terminated, normally or
     * abnormally.
     *
     * @param script
     *            {@link Script} to execute
     * @param paramArray
     *            Array of parameters to pass to the {@link Script}
     * @param handler
     *            {@link ScriptHandler} to be invoke upon
     *            {@link Script} termination.
     * @param debugger
     *            {@link ScriptDebugger} to invoke when the execution hits
     *            a breakpoint
     * @return {@link Runner} object for controlling {@link Script}
     *         execution
     */
    public static synchronized Runner execute(Script script, ScriptParameter[]
        paramArray, ScriptHandler handler, ScriptDebugger debugger) {
        if (!running) {
            throw new RejectedExecutionException(
                    "Cannot start, Executor has been stopped");
        }

        log.debug("Starting script '" + script.getName() + "'");

        script = Conditions.checkNotNull(script, "script");
        paramArray = Conditions.checkNotNull(paramArray, "paramArray");
        handler = Conditions.checkNotNull(handler, "handler");

        Runner runner = new Runner(script, paramArray, handler, debugger);
        pool.submit(runner::execute);
        return runner;
    }

    /**
     * Resumes a previously suspended {@link Script}
     *
     * @param runner
     *            {@link Runner} object representing the suspended
     *            {@link Script}
     */
    public static synchronized void resume(final Runner runner) {
        if (!running) {
            throw new RejectedExecutionException(
                    "Cannot start, Executor has been stopped");
        }
        log.debug("Resuming script '" + runner.getScript().getName() + "'");
        pool.submit(runner::resume);
    }

    /**
     * Convenience method for creating a
     * {@link ValueExpression} that wraps a Java object.
     *
     * @param value
     *            The object to be wrapped
     * @param type
     *            Type of the object to be wrapped
     * @return {@link ValueExpression} wrapping a Java object
     */
    protected static ValueExpression createValueExpression(Object value,
            Class<?> type) {
        return expFct.createValueExpression(value, type);
    }

    /**
     * Evaluates an EL expression.
     *
     * @param context
     *            {@link ExecutionContext} to be used during evaluation
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
     *            {@link ExecutionContext} to be used during evaluation
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
        synchronized (context) {
            ELContext elContext = context.getELContext();
            ValueExpression result = expFct.createValueExpression(elContext,
                    expression, type);
            return (T) result.getValue(elContext);
        }
    }

}
