package org.dei.perla.core.fpc.base;

import org.dei.perla.core.engine.Executor;
import org.dei.perla.core.engine.Script;
import org.dei.perla.core.engine.ScriptHandler;
import org.dei.perla.core.fpc.FpcException;
import org.dei.perla.core.utils.StopHandler;

import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

public class SimulatedPeriodicOperation extends PeriodicOperation {

	private static final ScheduledThreadPoolExecutor executor;
	static {
		executor = new ScheduledThreadPoolExecutor(10);
		executor.setRemoveOnCancelPolicy(true);
	}

	private final Script script;

	private volatile ScheduledFuture<?> timerFuture = null;
	private final ScriptHandler timerScriptHandler = new TimerScriptHandler();

	public SimulatedPeriodicOperation(String id, Script script) {
		super(id, script.getEmit());
		this.script = script;
		timerFuture = null;
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
		timerFuture = executor.scheduleAtFixedRate(() -> {
					Executor.execute(script,
							Executor.EMPTY_PARAMETER_ARRAY,
							timerScriptHandler);
				},
				period, period, TimeUnit.MILLISECONDS);
	}

	@Override
	protected void doStop(StopHandler<Operation> handler) {
		executor.shutdownNow();
		handler.hasStopped(this);
	}

	private class TimerScriptHandler implements ScriptHandler {

		@Override
		public void complete(Script script, List<Object[]> samples) {
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
