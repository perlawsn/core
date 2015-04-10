package org.dei.perla.core.fpc.base;

import org.dei.perla.core.engine.Executor;
import org.dei.perla.core.engine.Runner;
import org.dei.perla.core.engine.Script;
import org.dei.perla.core.engine.ScriptHandler;
import org.dei.perla.core.fpc.FpcException;

import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class SimulatedPeriodicOperation extends PeriodicOperation {

	private final ScheduledThreadPoolExecutor executor;

	private final Script script;

	private volatile ScheduledFuture<?> timerFuture = null;
	private final ScriptHandler handler = new TimerScriptHandler();

	public SimulatedPeriodicOperation(String id, Script script) {
		super(id, script.getEmit());
		this.script = script;
		timerFuture = null;

		// A single executor thread, combined with the synchronous script
		// execution model (see the sample method), ensures that all script
		// invocations are executed sequentially. If multiple threads were
		// used instead the Java scheduler could reorder or aggregate
		// the single script executions, causing a more or less severe
		// alteration of the effective sampling period.
		executor = new ScheduledThreadPoolExecutor(1);
		executor.setRemoveOnCancelPolicy(true);
	}

	@Override
	protected void setSamplingPeriod(final long period) {
		if (timerFuture != null) {
			timerFuture.cancel(true);
		}
		if (period == 0) {
			currentPeriod = 0;
			return;
		}

		currentPeriod = period;
		forEachTask(t -> t.setInputPeriod(period));
		timerFuture = executor.scheduleAtFixedRate(this::sample,
				period, period, TimeUnit.MILLISECONDS);
	}

	private void sample() {
		try {
			Runner r = Executor.execute(script,
					Executor.EMPTY_PARAMETER_ARRAY, handler);
			// The synchronous execution of the sampling script ensures that
			// all script invocations are performed sequentially.
			r.await();
		} catch (Exception e) {
			handler.error(new RuntimeException("Unexpected error while " +
					"running simulated periodic operation", e));
		}
	}

	@Override
	protected void doStop(Consumer<Operation> handler) {
		executor.shutdownNow();
		handler.accept(this);
	}

	private class TimerScriptHandler implements ScriptHandler {


		@Override
		public void complete(Script script, List<Object[]>
				samples) {
				for (Object[] s : samples) {
					forEachTask(t -> t.newSample(s));
				}
		}

		@Override
		public void error(Throwable cause) {
				Exception e = new FpcException(cause);
				forEachTask(t -> t.notifyError(e, false));
		}

	}

}
