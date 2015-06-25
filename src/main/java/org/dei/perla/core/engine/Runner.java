package org.dei.perla.core.engine;

import org.apache.log4j.Logger;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>
 * A class for running {@link Script} {@code Runner} objects contain
 * various methods for controlling {@link Script} execution.
 *
 * <p>
 * {@link Script} execution can be monitored and influenced using a
 * {@link ScriptDebugger} which is invoked whenever a breakpoint
 * instruction is encountered. It is important to note that {@link Script}
 * debugging may severly impact on the overall system performance, and it is not
 * intended to be used in a production environment.
 * </p>
 *
 * @author Guido Rota (2014)
 *
 */
public class Runner {

    private static final Logger log = Logger.getLogger(Runner.class);

    // Caching mechanism to reuse ExecutionContext objects
    private static final Queue<ExecutionContext> contextPool = new ConcurrentLinkedQueue<>();

    // State values
    private static final int NEW = 0;
    private static final int RUNNING = 1;
    private static final int SUSPENDED = 2;
    private static final int STOPPED = 3;
    private static final int CANCELLED = 4;

    // Objects shared with instructions
    protected final ExecutionContext ctx;

    // Runner state variables
    private final Script script;
    private final ScriptHandler handler;
    private final ScriptDebugger debugger;
    private volatile Instruction instruction; // Program counter

    private boolean breakpoint;
    private int state;

    // Lock acquired when running the Script
    protected final Object runLock = new Object();

    protected Runner(Script script, ScriptParameter[] params,
            ScriptHandler handler, ScriptDebugger debugger) {
        this.script = script;
        this.handler = handler;
        this.debugger = debugger;

        this.breakpoint = false;
        state = NEW;
        this.ctx = getContext();
        this.ctx.init(script.getEmit().size(), params);
    }

    protected Script getScript() {
        return script;
    }

    /**
     * Returns an {@link ExecutionContext} taken from a pool of unused
     * contexts. The {@link ExecutionContext} object is cleared of all
     * previous information prior to return.
     *
     * @return {@link ExecutionContext} object
     */
    private static final ExecutionContext getContext() {
        synchronized (contextPool) {
            ExecutionContext context = contextPool.poll();
            if (context == null) {
                return new ExecutionContext();
            }
            return context;
        }
    }

    /**
     * Places an unused {@link ExecutionContext} in the pool.
     *
     * @param context {@link ExecutionContext} to be returned to the pool
     */
    private static final void relinquishContext(ExecutionContext context) {
        synchronized (contextPool) {
            context.clear();
            contextPool.add(context);
        }
    }

    /**
     * <p>
     * Suspends the execution of the {@link Script} being run by the {@code
     * Runner} Suspended {@code Runner} can be resumed using the {@code
     * Executor.resume()} method.
     *
     * <p>
     * A suspended {@code Runner} is not considered done. Invoking the {@code
     * getResult()} method on a suspended {@code Runner} will block until the
     * execution is resumed and completed.
     */
    protected synchronized void suspend() {
        if (state != RUNNING) {
            String msg = "Cannot suspend, Runner is not in execution";
            log.error(msg);
            throw new IllegalStateException(msg);
        }
        state = SUSPENDED;
    }

    /**
     * Stops the {@link Script}.
     */
    protected synchronized void stop() {
        if (state != RUNNING) {
            String msg = "Cannot stop, Runner is not in execution";
            log.error(msg);
            throw new IllegalStateException(msg);
        }
        try {
            handler.complete(script, ctx.getSamples());
        } catch (Exception e) {
            String msg = "Unexpected error in script '" + script.getName() +
                    "': exception occurred in ScriptHandler.complete() method";
            log.error(msg, e);
            handler.error(script, new ScriptException(msg, e));
        }
        state = STOPPED;
        relinquishContext(ctx);
    }

    /**
     * Cancels the {@link Script} execution. No samples are emitted upon
     * cancellation.
     */
    public synchronized void cancel() {
        if (state == CANCELLED || state == STOPPED) {
            return;
        }
        state = CANCELLED;
        relinquishContext(ctx);

        String msg = "Script '" + script.getName() + "' cancelled.";
        log.debug(msg);
        handler.error(script, new ScriptCancelledException(msg));
    }

    /**
     * Sets a breakpoint, causing the {@code Runner} to invoke the
     * {@link ScriptDebugger} (if any) before running the next instruction.
     */
    protected synchronized void setBreakpoint() {
        breakpoint = true;
    }

    /**
     * Indicates whether the <code>Runner</code> is suspended or not
     *
     * @return True if the <code>Runner</code> is suspended, false otherwise
     */
    public synchronized boolean isSuspended() {
        return state == SUSPENDED;
    }

    /**
     * Indicates whether the {@code Runner} has stopped or has been cancelled.
     *
     * @return True if the {@code Runner} has stopped or has been cancelled,
     * false otherwise
     */
    public synchronized boolean isDone() {
        return state >= STOPPED;
    }

    /**
     * <p>
     * Indicates whether the {@code Runner} has been cancelled or not.
     *
     * <p>
     * {@code Runner}s enter the cancelled state if they are explicitly
     * cancelled by a user through the {@code cancel()} method or if an
     * exception occurs during while the {@link Script} is executed. in
     * this latter case the exception that caused cancellation can be retrieved
     * with an invocation to the {@code getResult()} method.
     *
     * @return True if the {@code Runner} was cancelled, false otherwise
     */
    public synchronized boolean isCancelled() {
        return state == CANCELLED;
    }

    /**
     * Main execution method invoked by the {@link Executor} class to run
     * the {@link Script}.
     */
    protected void execute() {
        synchronized (this) {
            if (state != NEW) {
                String msg = "Cannot start, Runner has already been run";
                log.error(msg);
                throw new IllegalStateException(msg);
            }
            state = RUNNING;
            // Fetch the first instruction and start the run loop
            instruction = script.getCode();
        }

        run();
    }

    /**
     * Method invoked by the {@link Executor} class to resume a previously
     * suspended {@link Script}.
     */
    protected void resume() {
        synchronized (this) {
            if (state != SUSPENDED) {
                String msg = "Cannot resume, Runner is not in suspended state";
                log.error(msg);
                throw new IllegalStateException(msg);
            }
            state = RUNNING;
        }

        // No need to fetch the first instruction of the Script, since the
        // Runner is resuming from where it was previously interrupted
        run();
    }

    /**
     * Main {@link Script} execution loop.
     */
    private void run() {
        synchronized (runLock) {
            try {
                do {
                    synchronized (this) {
                        if (instruction == null && state == RUNNING) {
                            String msg = "Missing stop instruction in script '"
                                    + script.getName() + "'";
                            log.error(msg);
                            handler.error(script, new ScriptException(msg));
                        }
                        if (debugger != null && breakpoint) {
                            breakpoint = false;
                            debugger.breakpoint(this, script, instruction);
                        }
                    }

                    instruction = instruction.run(this);

                    synchronized (this) {
                        if (state != RUNNING) {
                            break;
                        }
                    }

                } while (true);
            } catch (Exception e) {
                synchronized (this) {
                    // Catching all Exceptions, since we don't want any error in the
                    // user's scripts or in the handler code to bring down the entire
                    // system
                    if (state == CANCELLED || state == STOPPED) {
                        return;
                    } else if (state == SUSPENDED) {
                        String msg = "FATAL: Runner should not receive exceptions" +
                                " while in SUSPENDED mode";
                        log.error(msg, e);
                        throw new IllegalStateException(msg, e);
                    }

                    state = CANCELLED;
                    relinquishContext(ctx);
                    String msg = "Unexpected error in script '" + script.getName() +
                            "', instruction '" + instruction.getClass().getSimpleName() + "'";
                    log.error(msg, e);
                    handler.error(script, new ScriptException(msg, e));
                }
            }
        }
    }

}
