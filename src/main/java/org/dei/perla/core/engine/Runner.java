package org.dei.perla.core.engine;

import org.apache.log4j.Logger;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>
 * A class for running <code>Script</code>s. <code>Runner</code> objects contain
 * various methods for controlling <code>Script</code> execution.
 * </p>
 *
 *
 * <p>
 * <code>Script</code> execution can be monitored and influenced using a
 * <code>ScriptDebugger</code>, which is invoked whenever a breakpoint
 * instruction is encountered. It is important to note that <code>Script</code>
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
	private Instruction instruction; // Program counter

	private boolean breakpoint;
	private final AtomicInteger state;

	protected Runner(Script script, ScriptParameter[] params,
			ScriptHandler handler, ScriptDebugger debugger) {
		this.script = script;
		this.handler = handler;
		this.debugger = debugger;

		this.breakpoint = false;
		this.state = new AtomicInteger(NEW);
		this.ctx = getContext();
		this.ctx.init(script.getEmit().size(), params);
	}

    protected Script getScript() {
        return script;
    }

	/**
	 * Returns an <code>ExecutionContext</code> taken from a pool of unused
	 * contexts. The <code>ExecutionContext</code> object is cleared of all
	 * previous information prior to return.
	 *
	 * @return <code>ExecutionContext</code> object
	 */
	private static final ExecutionContext getContext() {
		// No synchronization needed. Worst that can happen is that we create
		// some additional ExecutionContext objects
		ExecutionContext context = contextPool.poll();
		if (context == null) {
			return new ExecutionContext();
		}
		return context;
	}

	/**
	 * Places an unused <code>ExecutionContext</code> in the pool.
	 *
	 * @param context
	 *            <code>ExecutionContext</code> to be returned to the pool
	 */
	private static final void relinquishContext(ExecutionContext context) {
        context.clear();
		contextPool.add(context);
	}

	/**
	 * <p>
	 * Suspends the execution of the <code>Script</code> being run by the
	 * <code>Runner</code>. Suspended <code>Runners</code> can be resumed using
	 * the <code>Executor.resume()</code> method.
	 *
	 * <p>
	 * A suspended <code>Runner</code> is not considered done. Invoking the
	 * <code>getResult()</code> method on a suspended <code>Runner</code> will
	 * block until the execution is resumed and completed.
	 */
	protected void suspend() {
		state.compareAndSet(RUNNING, SUSPENDED);
	}

	/**
	 * Stops the <code>Script</code>.
	 */
	protected void stop() {
		if (!state.compareAndSet(RUNNING, STOPPED)) {
			return;
		}
		handler.complete(script, ctx.getSamples());
		relinquishContext(ctx);
	}

	/**
	 * Cancels the {@link Script} execution. No samples are emitted upon
	 * cancellation.
	 */
	public void cancel() {
        int old;
        do {
            old = state.get();
            if (old == CANCELLED || old == STOPPED) {
                return;
            }
        } while (!state.compareAndSet(old, CANCELLED));
        relinquishContext(ctx);
        handler.error(new ScriptCancelledException("Script '"
                + script.getName() + "' cancelled."));
	}

	/**
	 * Sets a breakpoint, causing the <code>Runner</code> to invoke the
	 * <code>ScriptDebugger</code> (if any) before running the next instruction.
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
		return state.get() == SUSPENDED;
	}

	/**
	 * Indicates whether the <code>Runner</code> has stopped or has been
	 * cancelled.
	 *
	 * @return True if the <code>Runner</code> has stopped or has been
	 *         cancelled, false otherwise
	 */
	public boolean isDone() {
		return state.get() >= STOPPED;
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
		return state.get() == CANCELLED;
	}

    /**
     * Method invoked by the {@link Executor} class to resume a previously
     * suspended {@link Script}.
     */
	protected void resume() {
		if (!state.compareAndSet(SUSPENDED, RUNNING)) {
            throw new IllegalStateException(
                    "Cannot resume, Runner is not in suspended state");
		}
		run();
	}

	/**
	 * Main execution method invoked by the {@link Executor} class to run
	 * the {@link Script}.
	 */
	protected void execute() {
		if (!state.compareAndSet(NEW, RUNNING)) {
            throw new IllegalStateException(
                    "Cannot start, Runner has already been run");
		}
		instruction = script.getCode();
		run();
	}

	private void run() {
		try {
			do {
				if (instruction == null) {
					state.set(CANCELLED);
					handler.error(new ScriptException(
							"Missing stop instruction in script '"
									+ script.getName() + "'"));
				}

				if (debugger != null && breakpoint) {
					breakpoint = false;
					debugger.breakpoint(this, script, instruction);
				}
				instruction = instruction.run(this);
			} while (state.get() == RUNNING);
		} catch (Throwable t) {
            // Catching Throwable, since we don't want any error in the
            // user's scripts to bring down the entire system
			state.set(CANCELLED);
			relinquishContext(ctx);
			String msg = "Unexpected error in script '" + script.getName() +
					"', instruction '" + instruction.getClass().getSimpleName() + "'";
			log.error(msg, t);
			handler.error(new ScriptException(msg, t));
		}
	}

}
