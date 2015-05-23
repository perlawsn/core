package org.dei.perla.core.fpc.base;

import org.apache.log4j.Logger;
import org.dei.perla.core.engine.Executor;
import org.dei.perla.core.engine.Runner;
import org.dei.perla.core.engine.Script;
import org.dei.perla.core.engine.ScriptHandler;
import org.dei.perla.core.fpc.TaskHandler;
import org.dei.perla.core.sample.SamplePipeline;

import java.util.List;

/**
 * <p>
 * An object for controlling the asynchronous execution of a single
 * {@link Script}. {@code ScriptTask}s are used to control Set or Get operation;
 * as such, they terminate as soon as the associated {@link Script} stops.
 * </p>
 *
 * <p>
 * All {@link Sample}s generated by the {@link Script} are notified to the
 * interested components through one or more invocations of the
 * {@link TaskHandler} passed as parameter.
 * </p>
 *
 * @author Guido Rota (2014)
 *
 */
public class ScriptTask extends AbstractTask {

	private final Logger log;
	private final Runner runner;

	protected ScriptTask(OneoffOperation op, TaskHandler h, SamplePipeline p) {
		super(op, h, p);
		log = Logger.getLogger(op.getId() + " one-off Operation");

		ScriptHandler scriptHand = new OneoffScriptHandler();
		this.runner = Executor.execute(op.getScript(), scriptHand);
	}

	@Override
	public void doStop() {
		runner.cancel();
	}

	/**
	 * Custom {@link ScriptHandler} used for collecting the results of the
	 * {@link Script} and for switching the {@code ScriptTask} state (running /
	 * not running).
	 *
	 * @author Guido Rota (2014)
	 *
	 */
	private class OneoffScriptHandler implements ScriptHandler {

		@Override
		public void complete(Script script, List<Object[]> samples) {
			try {
				samples.forEach(ScriptTask.this::processSample);
				notifyComplete();
			} catch (Exception e) {
				String msg = "Error while running operation handler";
				log.error(msg, e);
				notifyError(e, true);
			}
		}

		@Override
		public void error(Throwable cause) {
			notifyError(cause, true);
		}

	}

}
