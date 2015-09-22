package org.dei.perla.core.fpc.base;

import org.dei.perla.core.engine.Executor;
import org.dei.perla.core.engine.Script;
import org.dei.perla.core.engine.ScriptHandler;
import org.dei.perla.core.engine.ScriptParameter;
import org.dei.perla.core.fpc.FpcException;
import org.dei.perla.core.message.FpcMessage;
import org.dei.perla.core.fpc.Attribute;
import org.dei.perla.core.utils.Check;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public final class NativePeriodicOperation extends PeriodicOperation {

    // Operation states
    private static final int STOPPED = 0;
    private static final int STARTING = 1;
    private static final int RUNNING = 2;

    private final Script start;
    private final Script stop;

    private final ChannelManager chanMgr;
    private final Map<String, OnScriptHandler> handlers = new HashMap<>();

    // Operation state
    private volatile int state = 0;

    // Current sample, used to merge the results from different async messages
    // The currentSample contains an up-to-date view on the most recent value
    // of all attributes managed by the Operation
    private final Object[] currentSample;

    public NativePeriodicOperation(String id, List<Attribute> atts,
            Script start, Script stop, List<MessageScript> msgs,
            ChannelManager chanMgr) {
        super(id, atts);
        this.start = start;
        this.stop = stop;
        this.chanMgr = chanMgr;

        int nAtt = 0;
        for (MessageScript m : msgs) {
            OnScriptHandler osh = new OnScriptHandler(m);
            handlers.put(m.getMapper().getMessageId(), osh);
            nAtt += m.getScript().getEmit().size();
        }
        currentSample = new Object[nAtt];
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
     *
     * <p>
     * The {@code STARTING} state guarantees that only a single start
     * {@link Script} may be running at any given time, even when multiple
     * sampling tasks are requested concurrently.
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

        switch (state) {
            case STOPPED:
                state = STARTING;
                runStartScript(period);
                currentPeriod = period;
                break;
            case STARTING:
                // Operation is still starting, just change the currentPeriod so
                // that the starting handler can update the sampling
                currentPeriod = period;
                break;
            case RUNNING:
                state = STARTING;
                currentPeriod = period;
                runStopScript(); // stop handler will update the sampling period
                break;
            default:
                throw new RuntimeException("Unknown state " + state);
        }
    }

    @Override
    protected void doStop(Consumer<Operation> handler) {
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

    public void handleMessage(FpcMessage message) {
        OnScriptHandler h = handlers.get(message.getId());
        MessageScript ms = h.msgs;

        ScriptParameter paramArray[] = new ScriptParameter[1];
        paramArray[0] = new ScriptParameter(ms.getVariable(), message);

        Executor.execute(ms.getScript(), paramArray, h);
    }

    /**
     * Handler for managing the {@link Script} that starts this operation. This
     * handler checks for sampling rate changes requested while the starting
     * script was still being executed, and reboots the sampling operation to
     * comply with the new rate.
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
        public void complete(Script script, List<Object[]> samples) {
            synchronized (NativePeriodicOperation.this) {
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
            }
        }

        private void addAsyncCallback() {
            for (OnScriptHandler h : handlers.values()) {
                chanMgr.addCallback(h.msgs.getMapper(),
                        NativePeriodicOperation.this::handleMessage);
            }
        }

        @Override
        public void error(Script script, Throwable cause) {
            synchronized (NativePeriodicOperation.this) {
                unrecoverableError("Cannot start operation '" + getId() + "'",
                        cause);
            }
        }

    }

    /**
     * <p>
     * Handler for managing the {@link Script} that stops this operation. This
     * handler is responsible for restarting the sampling activity if the
     * sampling rate of the operation is different than zero.
     *
     * <p>
     * This may happen when a new sampling task is requested while the stopping
     * script is executing or if the stop script was invoked to restart the
     * sampling operation.
     *
     * @author Guido Rota (2014)
     *
     */
    private class StopScriptHandler implements ScriptHandler {

        private final Consumer<Operation> stopHandler;

        private StopScriptHandler() {
            this(null);
        }

        private StopScriptHandler(Consumer<Operation> stopHandler) {
            this.stopHandler = stopHandler;
        }

        @Override
        public void complete(Script script, List<Object[]> samples) {
            synchronized (NativePeriodicOperation.this) {
                if (currentPeriod != 0) {
                    // Restart the operation if the sampling period changed
                    // while the stop script was running
                    state = STARTING;
                    runStartScript(currentPeriod);

                } else {
                    state = STOPPED;
                    removeAsyncCallback();
                    if (stopHandler != null) {
                        stopHandler.accept(NativePeriodicOperation.this);
                    }
                }
            }
        }

        private void removeAsyncCallback() {
            for (OnScriptHandler h : handlers.values()) {
                chanMgr.removeCallback(h.msgs.getMapper());
            }
        }

        @Override
        public void error(Script script, Throwable cause) {
            synchronized (NativePeriodicOperation.this) {
                if (stopHandler == null) {
                    unrecoverableError("Cannot stop operation", cause);
                }

                // Notify caller if stopHandler was set
                if (stopHandler != null) {
                    stopHandler.accept(NativePeriodicOperation.this);
                }
            }
        }

    }

    /**
     * Handler for managing the 'on' {@link Script} (sample creation from
     * asynchronous message)
     *
     * @author Guido Rota (2014)
     *
     */
    private class OnScriptHandler implements ScriptHandler {

        private final MessageScript msgs;

        private OnScriptHandler(MessageScript msgs) {
            this.msgs = msgs;
        }

        @Override
        public void complete(Script script, List<Object[]> samples) {
            if (Check.nullOrEmpty(samples)) {
                return;
            }

            // Merge only short-circuit: don't even acquire a lock to the
            // Operation if the only thing that needs to be done is merging
            // the data
            if (handlers.size() != 1 && !msgs.isSync()) {
                synchronized (currentSample) {
                    // We only care about the last sample when merging
                    int lastIndex = samples.size() - 1;
                    Object[] last = samples.get(lastIndex);
                    merge(last);
                }
                return;
            }

            synchronized (NativePeriodicOperation.this) {
                if (handlers.size() == 1) {
                    // Distribute immediately to the Tasks if the operation only
                    // receives one message type. Doing so avoids the cost of
                    // merging with the current sample
                    for (Object[] s : samples) {
                        forEachTask(t -> t.newSample(s));
                    }

                } else if (msgs.isSync()) {
                    synchronized (currentSample) {
                        // Merge with the current sample and distribute
                        for (Object[] s : samples) {
                            merge(s);
                            forEachTask(t -> t.newSample(currentSample));
                        }
                    }
                }
            }
        }

        /**
         * Merges the samples received from the {@link Script} with the
         * currentSample.
         *
         * @param s sample to merge
         */
        private void merge(Object[] s) {
            System.arraycopy(s, 0, currentSample, msgs.getBase(), s.length);
        }

        @Override
        public void error(Script script, Throwable cause) {
            synchronized (NativePeriodicOperation.this) {
                Exception e = new FpcException(cause);
                forEachTask(t -> error(script, e));
            }
        }

    }

}
