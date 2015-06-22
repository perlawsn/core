package org.dei.perla.core.fpc.base;

import org.apache.log4j.Logger;
import org.dei.perla.core.fpc.FpcException;
import org.dei.perla.core.fpc.Task;
import org.dei.perla.core.fpc.TaskHandler;
import org.dei.perla.core.sample.Attribute;
import org.dei.perla.core.sample.Sample;
import org.dei.perla.core.sample.SamplePipeline;

import java.util.List;

/**
 * An abstract implementation of the {@link Task} interface. It is the base
 * class implemented by all {@link Task}s scheduled from an {@link
 * AbstractOperation}.
 *
 * @author Guido Rota (2014)
 *
 */
public abstract class AbstractTask implements Task {

	protected final Logger log;

	private boolean running = true;
	private final AbstractOperation<? extends AbstractTask> op;
	private final SamplePipeline pipeline;
	private final List<Attribute> atts;
	private final TaskHandler handler;

	/**
	 * Instantiates a new {@code AbstractTask}.
	 *
	 * @param op
	 *            {@link AbstractOperation} from which this {@code AbstractTask}
	 *            was scheduled
	 * @param handler
	 *            {@link TaskHandler} employed to notify the presence of new
	 *            {@link Sample}s to other interested users.
	 * @param pipeline
	 *            {@link SamplePipeline} used to process new {@link Sample}
	 *            prior to notifying them to the {@link TaskHandler}.
	 */
	public AbstractTask(AbstractOperation<? extends AbstractTask> op,
			TaskHandler handler, SamplePipeline pipeline) {
		this.op = op;
		this.handler = handler;
		this.pipeline = pipeline;
		this.atts = pipeline.atts;

		log = Logger.getLogger(op.getClass().getSimpleName() + " task");
	}

	@Override
	public final List<Attribute> getAttributes() {
		return atts;
	}

	@Override
	public final synchronized boolean isRunning() {
		return running;
	}

	/**
	 * Returns the {@link Operation} used to schedule this {@link Task}
	 *
	 * @return {@link Operation} that scheduled this {@link Task}
	 */
	protected final Operation getOperation() {
		return op;
	}

	@Override
	public final synchronized void stop() {
		if (!running) {
			return;
		}
		running = false;
		doStop();
		op.remove(this);
		handler.complete(this);
	}

	/**
	 * A method invoked whenever the {@code AbstractTask} is stopped. It can be
	 * overridden by concrete {@code AbstractTask} implementation to add custom
	 * shutdown behaviour.
	 */
	protected void doStop() {}


	//////////////////////////////////////
	// Methods invoked by parent Operation
	//////////////////////////////////////

	/**
	 * Immediately cancels the {@link AbstractTask} execution following an error
	 * occurred to the connected {@link Operation}.
	 *
	 * <p>
	 * This method is intended to be called by an {@link Operation} object to
	 * indicate that an exception occurred while processing a script. Invoking
	 * this method will not remove the task from the {@link AbstractOperation}'s
	 * task list.
	 *
	 * <p>
	 * Invoking this method does not produce any effect if the
	 * {@code AbstractTask} is stopped
	 *
	 * @param cause
	 *            Cause of the error
	 */
	protected final synchronized void operationError(FpcException cause) {
		if (!running) {
			return;
		}
		running = false;
		doStop();
		handler.error(this, cause);
	}

	/**
	 * Immediately stops the {@link AbstractTask} execution.
	 *
	 * <p>
	 * This method is intended to be called by an {@link AbstractOperation}
	 * object to indicate that no more samples will be produced. Invoking this
	 * method will not remove the task from the {@link AbstractOperation}'s task
	 * list.
	 *
	 * <p>
	 * Invoking this method does not produce any effect if the
	 * {@code AbstractTask} is stopped
	 */
	protected final synchronized void operationStopped() {
		if (!running) {
			return;
		}
		running = false;
		doStop();
		handler.complete(this);
	}


	///////////////////////////////////////////
	// Methods invoked by AbstractTask children
	///////////////////////////////////////////

	/**
	 * Runs the a new {@link Sample} in the {@link SamplePipeline} and handles
	 * it over to the registered {@link TaskHandler}. This method is intended to
	 * be invoked by a {@link AbstractOperation} whenever a new sample is
	 * produced by the remote device.
	 *
	 * <p>
	 * Invoking this method does not produce any effect if the
	 * {@code AbstractTask} is stopped
	 *
	 * @param sample
	 *            sample to be processed
	 */
	protected final synchronized void processSample(Object[] sample) {
		if (!running) {
			return;
		}
		Sample output = pipeline.run(sample);
        handler.data(this, output);
	}

	/**
	 * Invokes the registered {@link TaskHandler} to inform any interested
	 * object that the {@link Task} is complete, and that no new {@link Sample}
	 * are going to be produced.
	 *
	 * <p>
	 * This method is intended to be used by an AbstractTask subclass
	 *
	 * <p>
	 * Invoking this method does not produce any effect if the
	 * {@code AbstractTask} is stopped
	 */
	protected final synchronized void notifyComplete() {
		if (!running) {
			return;
		}
		running = false;
		op.remove(this);
		handler.complete(this);
	}

	/**
	 * Invokes the registered {@link TaskHandler} to inform any interested
	 * object that an error has occurred. The additional {@code stop} parameter
	 * may be used to indicate whether the error is unrecoverable (no new
	 * {@link Sample}s will be produced) or not.
	 *
	 * <p>
	 * This method is intended to be used by an AbstractTask subclass
	 *
	 * <p>
	 * Invoking this method does not produce any effect if the
	 * {@code AbstractTask} is stopped
	 *
	 * @param cause
	 *            Cause of the error
	 * @param stop
	 *            Stops the {@link AbstractTask} if set to true
	 */
	protected final synchronized void notifyError(Throwable cause, boolean stop) {
		if (!running) {
			return;
		}
		if (stop && running) {
			running = false;
			op.remove(this);
		}
		handler.error(this, cause);
	}

}
