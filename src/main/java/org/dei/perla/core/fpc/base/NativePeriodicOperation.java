package org.dei.perla.core.fpc.base;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dei.perla.core.fpc.Attribute;
import org.dei.perla.core.fpc.FpcException;
import org.dei.perla.core.fpc.engine.Executor;
import org.dei.perla.core.fpc.engine.Record;
import org.dei.perla.core.fpc.engine.Script;
import org.dei.perla.core.fpc.engine.ScriptHandler;
import org.dei.perla.core.fpc.engine.ScriptParameter;
import org.dei.perla.core.message.FpcMessage;
import org.dei.perla.core.message.Mapper;
import org.dei.perla.core.utils.Check;
import org.dei.perla.core.utils.StopHandler;

public class NativePeriodicOperation extends PeriodicOperation {

	// Operation states
	private static final int STOPPED = 0;
	private static final int STARTING = 1;
	private static final int RUNNING = 2;

	private final Script startScript;
	private final Script stopScript;

	private final ChannelManager channelMgr;
	private final List<PeriodicMessageHandler> asyncHandlerList;
	private final Map<String, PeriodicMessageHandler> asyncHandlerMap = new HashMap<>();

	// Operation state
	private volatile int state = 0;

	// Current record, used to merge the results from different async messages
	private volatile Record currentRecord = Record.EMPTY;

	public NativePeriodicOperation(String id, Set<Attribute> attributeSet,
			Script startScript, Script stopScript,
			List<PeriodicMessageHandler> handlerList, ChannelManager channelMgr) {
		super(id, attributeSet);
		this.startScript = startScript;
		this.stopScript = stopScript;
		this.channelMgr = channelMgr;

		asyncHandlerList = handlerList;
		for (PeriodicMessageHandler handler : handlerList) {
			asyncHandlerMap.put(handler.mapper.getMessageId(), handler);
			handler.onHandler = new OnScriptHandler(handler.sync);
		}
	}

	public Script getStartScript() {
		return startScript;
	}

	public Script getStopScript() {
		return stopScript;
	}

	/**
	 * <p>
	 * Sets the sampling period of the current {@link Operation}. This method is
	 * responsible for starting the sampling operation and for changing the
	 * sampling period.
	 * </p>
	 *
	 * <p>
	 * The {@code STARTING} state guarantees that only a single start
	 * {@link Script} may be running at any given time, even when multiple
	 * sampling tasks are requested concurrently.
	 * </p>
	 *
	 *
	 * @param period
	 *            sampling period to be set
	 */
	@Override
	protected void setSamplingPeriod(long period) {
		// Check stop conditions
		if (period == 0 && currentPeriod == 0) {
			return;
		} else if (period == 0 && currentPeriod != 0) {
			currentPeriod = 0;
			runStopScript();
			return;
		}

		if (state == STOPPED) {
			state = STARTING;
			runStartScript(period);
			currentPeriod = period;
			return;

		} else if (state == STARTING) {
			currentPeriod = period;
			return;

		} else if (state == RUNNING) {
			state = STARTING;
			currentPeriod = period;
			runStopScript();
			return;
		}
	}

	@Override
	protected void doStop(org.dei.perla.core.utils.StopHandler<Operation> handler) {
		Executor.execute(stopScript, new StopScriptHandler(handler));
	}

	// Convenience method for running the start script
	private void runStartScript(long period) {
		ScriptParameter[] paramArray = new ScriptParameter[1];
		paramArray[0] = new ScriptParameter("period", period);
		Executor.execute(startScript, paramArray, new StartScriptHandler(period));
	}

	// Convenience method for running the stop script
	private void runStopScript() {
		Executor.execute(stopScript, new StopScriptHandler());
	}

	private void addAsyncCallback() {
		for (PeriodicMessageHandler asyncHandler : asyncHandlerList) {
			channelMgr.addCallback(asyncHandler.mapper, this::handleMessage);
		}
	}

	private void removeAsyncCallback() {
		for (PeriodicMessageHandler asyncHandler : asyncHandlerList) {
			channelMgr.removeCallback(asyncHandler.mapper);
		}
	}

	public void handleMessage(FpcMessage message) {
		PeriodicMessageHandler handler = asyncHandlerMap.get(message.getId());

		ScriptParameter paramArray[] = new ScriptParameter[1];
		paramArray[0] = new ScriptParameter(handler.variable, message);

		Executor.execute(handler.script, paramArray, handler.onHandler);
	}

	/**
	 * <p>
	 * Handler for managing the {@link Script} that starts this operation. This
	 * handler checks for sampling rate changes requested while the starting
	 * script was still being executed, and reboots the sampling operation to
	 * comply with the new rate.
	 * </p>
	 *
	 * @author Guido Rota (2014)
	 *
	 */
	private class StartScriptHandler implements ScriptHandler {

		private final long requestedPeriod;

		public StartScriptHandler(long period) {
			this.requestedPeriod = period;
		}

		@Override
		public void complete(List<Record> result) {
			runUnderLock(() -> {
				state = RUNNING;

				if (currentPeriod < requestedPeriod) {
					// Stop the current sampling operation and let the stop
					// handler recognize that it has to restart the sampling
					// operation with a different sampling period
					runStopScript();

				} else {
					addAsyncCallback();
					forEachTask(t -> t.setInputPeriod(currentPeriod));
				}
			});
		}

		@Override
		public void error(Throwable cause) {
			unrecoverableError("Cannot start operation '" + getId() + "'",
					cause);
		}

	}

	/**
	 * <p>
	 * Handler for managing the {@link Script} that stops this operation. This
	 * handler is responsible for restarting the sampling activity if the
	 * sampling rate of the operation is different than zero.
	 * </p>
	 *
	 * <p>
	 * This may happen when a new sampling task is requested while the stopping
	 * script is executing or if the stop script was invoked to restart the
	 * sampling operation.
	 * </p>
	 *
	 * @author Guido Rota (2014)
	 *
	 */
	private class StopScriptHandler implements ScriptHandler {

		private final StopHandler<Operation> stopHandler;

		private StopScriptHandler() {
			this(null);
		}

		private StopScriptHandler(StopHandler<Operation> stopHandler) {
			this.stopHandler = stopHandler;
		}

		@Override
		public void complete(List<Record> result) {
			runUnderLock(() -> {
				if (currentPeriod != 0) {
					// Restart the operation if the sampling period changed
					// while the stop script was running
					state = STARTING;
					runStartScript(currentPeriod);

				} else {
					state = STOPPED;
					removeAsyncCallback();
					if (stopHandler != null) {
						stopHandler.hasStopped(NativePeriodicOperation.this);
					}
				}
			});
		}

		@Override
		public void error(Throwable cause) {
			if (stopHandler == null) {
				unrecoverableError("Cannot stop operation", cause);
			}

			// Force stop
			stopHandler.hasStopped(NativePeriodicOperation.this);
		}
	}

	/**
	 * Handler for managing the 'on' <code>Script</code> (record creation from
	 * asynchronous message)
	 *
	 * @author Guido Rota (2014)
	 *
	 */
	private class OnScriptHandler implements ScriptHandler {

		private final boolean sync;

		private OnScriptHandler(boolean sync) {
			this.sync = sync;
		}

		@Override
		public void complete(List<Record> recordList) {
			if (Check.nullOrEmpty(recordList)) {
				return;
			}

			if (asyncHandlerList.size() == 1) {
				// Distribute immediately to the Tasks if the operation only
				// receives one message type. Doing so avoids the cost of
				// merging with the currentRecord
				distributeImmediately(recordList);

			} else if (sync == true) {
				// Merge with the current record and distribute
				mergeAndDistribute(recordList);

			} else {
				// We only care about the last record if we only have to merge
				int lastIndex = recordList.size() - 1;
				Record last = recordList.get(lastIndex);
				currentRecord = currentRecord.merge(last);
			}
		}

		private void distributeImmediately(List<Record> recordList) {
			for (Record record : recordList) {
				forEachTask(t -> t.newRecord(record));
			}
		}

		private void mergeAndDistribute(List<Record> recordList) {
			for (Record record : recordList) {
				currentRecord = Record.merge(currentRecord, record);
				forEachTask(t -> t.newRecord(currentRecord));
			}
		}

		@Override
		public void error(Throwable cause) {
			Exception e = new FpcException(cause);
			forEachTask(t -> error(e));
		}

	}

	public static class PeriodicMessageHandler {

		private final Mapper mapper;
		private final boolean sync;
		private final String variable;
		private final Script script;

		private OnScriptHandler onHandler;

		public PeriodicMessageHandler(boolean sync, Mapper mapper,
				String variable, Script script) {
			this.mapper = mapper;
			this.sync = sync;
			this.variable = variable;
			this.script = script;
		}

	}

}
