package org.dei.perla.core.engine;

import org.apache.log4j.Logger;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * <p>
 * A class for running <code>Script</code>s. <code>Runner</code> objects contain
 * various methods for controlling <code>Script</code> execution.
 *
 *
 * <p>
 * <code>Script</code> execution can be monitored and influenced using a
 * <code>ScriptDebugger</code>, which is invoked whenever a breakpoint
 * instruction is encountered. It is important to note that <code>Script</code>
 * debugging may severly impact on the overall system performance, and it is not
 * intended to be used in a production environment.
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
    private Instruction instruction; // Program counter
    private volatile boolean breakpoint;
    private int state;

    private final Lock stateLk = new ReentrantLock();
    private final Lock runLk = new ReentrantLock();

    protected Runner(Script script, ScriptParameter[] params,
            ScriptHandler handler, ScriptDebugger debugger) {
        this.script = script;
        this.handler = handler;
        this.debugger = debugger;

        this.breakpoint = false;
        this.ctx = getContext();
        this.ctx.init(script.getEmit().size(), params);
        state = NEW;
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
        // No synchronization needed. Worst that can happen is that we create
        // some additional ExecutionContext objects
        ExecutionContext context = contextPool.poll();
        if (context == null) {
            return new ExecutionContext();
        }
        context.clear();
        return context;
    }

    /**
     * Places an unused {@link ExecutionContext} in the pool.
     *
     * @param context {@link ExecutionContext} to be returned to the pool
     */
    private static final void relinquishContext(ExecutionContext context) {
        contextPool.add(context);
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
    protected void suspend() {
        stateLk.lock();
        try {
            if (state != RUNNING) {
                String msg = "Cannot suspend, Runner is not running";
                log.error(msg);
                throw new IllegalStateException(msg);
            }
            state = SUSPENDED;
        } finally {
            stateLk.unlock();
        }
    }

    /**
     * Stops the {@link Script}.
     */
    protected void stop() {
        stateLk.lock();
        try {
            if (state != RUNNING) {
                String msg = "Cannot stop, Runner is not running";
                log.error(msg);
                throw new IllegalStateException(msg);
            }
            state = STOPPED;
            try {
                handler.complete(script, ctx.getSamples());
            } catch (Exception e) {
                String msg = "Unexpected error in script '" + script.getName() +
                        "': exception occurred in ScriptHandler.complete() method";
                log.error(msg, e);
                handler.error(script, new ScriptException(msg, e));
            }
            relinquishContext(ctx);
        } finally {
            stateLk.unlock();
        }
    }

    /**
     * Cancels the {@link Script} execution. No samples are emitted upon
     * cancellation.
     */
    public void cancel() {
        stateLk.lock();
        try {
            if (state == CANCELLED || state == STOPPED) {
                return;
            }
            state = CANCELLED;
            String msg = "Script '" + script.getName() + "' cancelled.";
            log.debug(msg);
            handler.error(script, new ScriptCancelledException(msg));
            relinquishContext(ctx);
        } finally {
            stateLk.unlock();
        }
    }

    /**
     * Sets a breakpoint, causing the {@code Runner} to invoke the
     * {@link ScriptDebugger} (if any) before running the next instruction.
     */
    protected void setBreakpoint() {
        breakpoint = true;
    }

    /**
     * Indicates whether the <code>Runner</code> is suspended or not
     *
     * @return True if the <code>Runner</code> is suspended, false otherwise
     */
    public boolean isSuspended() {
        stateLk.lock();
        try {
            return state == SUSPENDED;
        } finally {
            stateLk.unlock();
        }
    }

    /**
     * Indicates whether the <code>Runner</code> has stopped or has been
     * cancelled.
     *
     * @return True if the <code>Runner</code> has stopped or has been
     *         cancelled, false otherwise
     */
    public boolean isDone() {
        stateLk.lock();
        try {
            return state >= STOPPED;
        } finally {
            stateLk.unlock();
        }
    }

    /**
     * <p>
     * Indicates whether the <code>Runner</code> has been cancelled or not.
     * </p>
     *
     * <p>
     * <code>Runner</code>s enter the cancelled state if they are explicitly
     * cancelled by a user through the <code>cancel()</code> method or if an
     * exception occurs during while the <code>Script</code> is executed. in
     * this latter case the exception that caused cancellation can be retrieved
     * with an invocation to the <code>getResult()</code> method.
     * </p>
     *
     * @return True if the <code>Runner</code> was cancelled, false otherwise
     */
    public boolean isCancelled() {
        stateLk.lock();
        try {
            return state == CANCELLED;
        } finally {
            stateLk.unlock();
        }
    }

    /**
     * Method invoked by the {@link Executor} class to resume a previously
     * suspended {@link Script}.
     */
    protected void resume() {
        runLk.lock();
        try {
            stateLk.lock();
            try {
                if (state != SUSPENDED) {
                    String msg = "Cannot resume, Runner is not in suspended state";
                    log.error(msg);
                    throw new IllegalStateException(msg);
                }
                state = RUNNING;
            } finally {
                stateLk.unlock();
            }

            // No need to fetch the first instruction of the Script, since the
            // Runner is resuming from where it was previously interrupted
            run();
        } finally {
            runLk.unlock();
        }
    }

    /**
     * Main execution method invoked by the {@link Executor} class to run
     * the {@link Script}.
     */
    protected void execute() {
        runLk.lock();
        try {
            stateLk.lock();
            try {
                if (state != NEW) {
                    String msg = "Cannot start, Runner has already been run";
                    log.error(msg);
                    throw new IllegalStateException(msg);
                }
                state = RUNNING;
            } finally {
                stateLk.unlock();
            }

            // Fetch the first instruction and start the run loop
            instruction = script.getCode();
            run();
        } finally {
            runLk.unlock();
        }
    }

    /**
     * Main {@link Script} execution loop.
     */
    private void run() {
        try {

            do {
                stateLk.lock();
                try {
                    if (state != RUNNING) {
                        break;
                    } else if (instruction == null && state == RUNNING) {
                        throw new ScriptException("Missing stop instruction in script '"
                                + script.getName() + "'");
                    }
                } finally {
                    stateLk.unlock();
                }

                if (debugger != null && breakpoint) {
                    breakpoint = false;
                    debugger.breakpoint(this, script, instruction);
                }

                instruction = instruction.run(this);

            } while (true);

        } catch (Exception e) {
            // Catching all Exceptions, since we don't want any error in the
            // user's scripts or in the handler code to bring down the entire
            // system
            stateLk.lock();
            try {
                state = CANCELLED;
            } finally {
                stateLk.unlock();
            }
            relinquishContext(ctx);
            String msg = "Unexpected error in script '" + script.getName() +
                    "', instruction '" + instruction.getClass().getSimpleName() + "'";
            log.error(msg, e);

            if (e instanceof UnsupportedPeriodException) {
                // Relay UnsupportedPeriodException as is, since it conveys
                // additional information that may be used by the handler to
                // choose an appropriate failure strategy
                handler.error(script, e);
            } else {
                // Wrap all other exceptionn in a ScriptException
                handler.error(script, new ScriptException(msg, e));
            }
        }
    }

}
