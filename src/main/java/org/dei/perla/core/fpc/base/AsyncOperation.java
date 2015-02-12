package org.dei.perla.core.fpc.base;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.dei.perla.core.fpc.Attribute;
import org.dei.perla.core.fpc.TaskHandler;
import org.dei.perla.core.engine.Executor;
import org.dei.perla.core.engine.Record;
import org.dei.perla.core.engine.Script;
import org.dei.perla.core.engine.ScriptHandler;
import org.dei.perla.core.engine.ScriptParameter;
import org.dei.perla.core.message.FpcMessage;
import org.dei.perla.core.message.Mapper;
import org.dei.perla.core.utils.StopHandler;

public class AsyncOperation extends AbstractOperation<AsyncOperation.AsyncTask> {

	private static final int STOPPED = 0;
	private static final int SUSPENDED = 1;
	private static final int STARTED = 2;

	private static final ScheduledThreadPoolExecutor executor;

	static {
		executor = new ScheduledThreadPoolExecutor(10);
		executor.setRemoveOnCancelPolicy(true);
	}

	private final Script startScript;

	private volatile int state;

	private final AsyncMessageHandler asyncHandler;
	private final OnHandler onHandler = new OnHandler();

	// Simulated operations
	private final AsyncPeriodicOperation asyncPeriodicOp;
	private final AsyncOneoffOperation asyncOneoffOp;

	private volatile Record record = Record.EMPTY;

	protected AsyncOperation(String id, Collection<Attribute> attributes,
			Script startScript, AsyncMessageHandler handler,
			ChannelManager channelMgr) {
		super(id, attributes);
		this.startScript = startScript;
		this.asyncHandler = handler;

		asyncPeriodicOp = new AsyncPeriodicOperation(id, attributes);
		asyncOneoffOp = new AsyncOneoffOperation(id, attributes);

		state = STARTED;
		runStartScript();
		channelMgr.addCallback(asyncHandler.mapper, this::handleMessage);
	}

	private void runStartScript() {
		if (startScript != null) {
			Executor.execute(startScript, new StartHandler());
		}
	}

	protected PeriodicOperation getAsyncPeriodicOperation() {
		return asyncPeriodicOp;
	}

	protected Operation getAsyncOneoffOperation() {
		return asyncOneoffOp;
	}

	@Override
	public AsyncTask doSchedule(Map<String, Object> parameterMap,
			TaskHandler handler, RecordPipeline pipeline)
			throws IllegalArgumentException {
		AsyncTask task = new AsyncTask(this, handler, pipeline);
		add(task);
		return task;
	}

	public void handleMessage(FpcMessage message) {
		ScriptParameter paramArray[] = new ScriptParameter[1];
		paramArray[0] = new ScriptParameter(asyncHandler.variable, message);

		Executor.execute(asyncHandler.script, paramArray, onHandler);
	}

	@Override
	public void doStop() {
		state = STOPPED;
	}

	@Override
	public void doStop(StopHandler<Operation> handler) {
		doStop();
		handler.hasStopped(this);
	}

	/**
	 *
	 * @author Guido Rota (2014)
	 *
	 */
	private class StartHandler implements ScriptHandler {

		@Override
		public void complete(List<Record> recordList) {
			// Does nothing, AsyncOperation are set as "STARTED" by default
		}

		@Override
		public void error(Throwable cause) {
			runUnderLock(() -> {
				if (state == SUSPENDED) {
					return;
				}
				state = SUSPENDED;
				String message = "Error starting asynchronous operation";
				log.error(message, cause);
				// Stop all periodic tasks
				asyncPeriodicOp.unrecoverableError(message, cause);
			});
		}

	}

	/**
	 *
	 * @author Guido Rota (2014)
	 *
	 */
	private class OnHandler implements ScriptHandler {

		@Override
		public void complete(List<Record> recordList) {
			recordList.forEach(r -> forEachTask(t -> t.processRecord(r)));
			int last = recordList.size() - 1;
			record = recordList.get(last);
		}

		@Override
		public void error(Throwable cause) {
			log.error("Execution error in 'on' script", cause);
			forEachTask(t -> t.notifyError(cause, false));
		}

	}

	public static class AsyncMessageHandler {

		private final Mapper mapper;
		private final Script script;
		private final String variable;

		public AsyncMessageHandler(Mapper mapper, Script script, String variable) {
			this.mapper = mapper;
			this.script = script;
			this.variable = variable;
		}

	}

	private class AsyncPeriodicOperation extends PeriodicOperation {

		private ScheduledFuture<?> timerFuture;

		public AsyncPeriodicOperation(String id,
				Collection<Attribute> attributes) {
			super(id, attributes);
		}

		@Override
		protected void setSamplingPeriod(final long period) {
			runUnderLock(() -> {
				if (timerFuture != null) {
					timerFuture.cancel(false);
				}
				if (period == 0) {
					currentPeriod = 0;
					return;
				}

				if (state == SUSPENDED) {
					// Try to reboot the async operation
					runStartScript();
					state = STARTED;
				}

				timerFuture = executor.scheduleAtFixedRate(
						() -> forEachTask(t -> t.newRecord(record)), 0, period,
						TimeUnit.MILLISECONDS);
				currentPeriod = period;
				forEachTask(t -> t.setInputPeriod(period));
			});
		}

		@Override
		protected void doStop(StopHandler<Operation> handler) {
			doStop();
			handler.hasStopped(this);
		}

	}

	private class AsyncOneoffOperation extends AbstractOperation<AsyncTask> {

		public AsyncOneoffOperation(String id, Collection<Attribute> attributes) {
			super(id, attributes);
		}

		@Override
		public AsyncTask doSchedule(Map<String, Object> parameterMap,
				TaskHandler handler, RecordPipeline pipeline)
				throws IllegalArgumentException {
			return runUnderLock(() -> {
				if (state == STOPPED) {
					throw new IllegalStateException("Opertion '" + getId()
							+ "' is stopped, cannot start new tasks");
				} else if (state == SUSPENDED) {
					// Try to reboot the async operation
					runStartScript();
					state = STARTED;
				}

				// We're not waiting for a record to be produced. Since this
				// operation runs asynchronously, we have no way to know when
				// new data will arrive
				AsyncTask task = new AsyncTask(this, handler, pipeline);
				task.processRecord(record);
				task.notifyComplete();
				return task;
			});
		}

		@Override
		protected void doStop() {
		}

		@Override
		protected void doStop(StopHandler<Operation> handler) {
			handler.hasStopped(this);
		}

	}

	protected class AsyncTask extends AbstractTask {

		public AsyncTask(AbstractOperation<?> operation, TaskHandler handler,
				RecordPipeline pipeline) {
			super(operation, handler, pipeline);
		}

	}

}
