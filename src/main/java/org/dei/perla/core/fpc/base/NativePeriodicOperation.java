package org.dei.perla.core.fpc.base;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.dei.perla.core.engine.Attribute;
import org.dei.perla.core.fpc.FpcException;
import org.dei.perla.core.engine.Executor;
import org.dei.perla.core.engine.Record;
import org.dei.perla.core.engine.Script;
import org.dei.perla.core.engine.ScriptHandler;
import org.dei.perla.core.engine.ScriptParameter;
import org.dei.perla.core.message.FpcMessage;
import org.dei.perla.core.message.Mapper;
import org.dei.perla.core.utils.Check;
import org.dei.perla.core.utils.StopHandler;

public class NativePeriodicOperation extends PeriodicOperation {

	// Operation states
	private static final int STOPPED = 0;
	private static final int STARTING = 1;
	private static final int RUNNING = 2;

	private final Script start;
	private final Script stop;

	private final ChannelManager chanMgr;
	private final Map<String, PeriodicMessageHandler> handlers =
            new HashMap<>();

	// Operation state
	private volatile int state = 0;

    private final List<Attribute> atts;

	// Current record, used to merge the results from different async messages
    Lock rlk = new ReentrantLock();
	private Object[] currentRecord;

	public NativePeriodicOperation(String id, List<Attribute> atts,
			Script start, Script stop, List<PeriodicMessageHandler> handlers,
            ChannelManager chanMgr) {
		super(id, atts);
		this.start = start;
		this.stop = stop;
		this.chanMgr = chanMgr;
        this.atts = atts;

        int nAtt = 0;
		for (PeriodicMessageHandler h : handlers) {
			this.handlers.put(h.mapper.getMessageId(), h);
			h.onHandler = new OnScriptHandler(h.sync, nAtt);
            nAtt += h.script.getEmit().size();
		}
        currentRecord = new Object[nAtt];
	}

	public Script getStartScript() {
		return start;
	}

	public Script getStopScript() {
		return stop;
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
		Executor.execute(stop, new StopScriptHandler(handler));
	}

	// Convenience method for running the start script
	private void runStartScript(long period) {
		ScriptParameter[] paramArray = new ScriptParameter[1];
		paramArray[0] = new ScriptParameter("period", period);
		Executor.execute(start, paramArray, new StartScriptHandler(period));
	}

	// Convenience method for running the stop script
	private void runStopScript() {
		Executor.execute(stop, new StopScriptHandler());
	}

	private void addAsyncCallback() {
		for (PeriodicMessageHandler h : handlers.values()) {
			chanMgr.addCallback(h.mapper, this::handleMessage);
		}
	}

	private void removeAsyncCallback() {
		for (PeriodicMessageHandler h : handlers.values()) {
			chanMgr.removeCallback(h.mapper);
		}
	}

	public void handleMessage(FpcMessage message) {
		PeriodicMessageHandler h = handlers.get(message.getId());

		ScriptParameter paramArray[] = new ScriptParameter[1];
		paramArray[0] = new ScriptParameter(h.variable, message);

		Executor.execute(h.script, paramArray, h.onHandler);
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

        private final int base;
		private final boolean sync;

		private OnScriptHandler(boolean sync, int base) {
			this.sync = sync;
            this.base = base;
		}

		@Override
		public void complete(List<Record> recordList) {
			if (Check.nullOrEmpty(recordList)) {
				return;
			}

			if (handlers.size() == 1) {
				// Distribute immediately to the Tasks if the operation only
				// receives one message type. Doing so avoids the cost of
				// merging with the currentRecord
                for (Record r : recordList) {
                    forEachTask(t -> t.newRecord(r));
                }

			} else if (sync == true) {
				// Merge with the current record and distribute
                for (Record r : recordList) {
                    Record m = merge(r);
                    forEachTask(t -> t.newRecord(m));
                }

			} else {
				// We only care about the last record if we only have to merge
				int lastIndex = recordList.size() - 1;
				Record last = recordList.get(lastIndex);
				merge(last);
			}
		}

        private Record merge(Record r) {
            Object[] values = r.getFields();

            rlk.lock();
            try {
                for (int i = 0; i < r.getAttributes().size(); i++) {
                    currentRecord[base + i] = values[i];
                }
                return new Record(atts, currentRecord);
            } finally {
                rlk.unlock();
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
