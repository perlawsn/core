package org.dei.perla.fpc.base;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.dei.perla.fpc.Attribute;
import org.dei.perla.fpc.FpcException;
import org.dei.perla.fpc.engine.Executor;
import org.dei.perla.fpc.engine.Record;
import org.dei.perla.fpc.engine.Script;
import org.dei.perla.fpc.engine.ScriptHandler;
import org.dei.perla.utils.StopHandler;

public class SimulatedPeriodicOperation extends PeriodicOperation {

	private static final ScheduledThreadPoolExecutor executor;

	static {
		executor = new ScheduledThreadPoolExecutor(10);
		executor.setRemoveOnCancelPolicy(true);
	}

	private final Script script;

	private ScheduledFuture<?> timerFuture = null;
	private final ScriptHandler timerScriptHandler;

	public SimulatedPeriodicOperation(String id, Set<Attribute> attributeSet,
			Script script) {
		super(id, attributeSet);
		this.script = script;

		timerFuture = null;
		timerScriptHandler = new TimerScriptHandler();
	}

	@Override
	protected void setSamplingPeriod(final long period) {
		if (timerFuture != null) {
			timerFuture.cancel(false);
		}
		if (period == 0) {
			currentPeriod = 0;
			return;
		}

		timerFuture = executor.scheduleAtFixedRate(() -> Executor.execute(
				script, Executor.EMPTY_PARAMETER_ARRAY, timerScriptHandler), 0,
				period, TimeUnit.MILLISECONDS);
		currentPeriod = period;
		forEachTask(t -> t.setInputPeriod(period));
	}
	
	@Override
	protected void doStop(StopHandler<Operation> handler) {
		handler.hasStopped(this);
	}

	private class TimerScriptHandler implements ScriptHandler {

		@Override
		public void complete(List<Record> recordList) {
			for (Record record : recordList) {
				forEachTask(t -> t.newRecord(record));
			}
		}

		@Override
		public void error(Throwable cause) {
			Exception e = new FpcException(cause);
			forEachTask(t -> t.notifyError(e, false));
		}

	}

}
