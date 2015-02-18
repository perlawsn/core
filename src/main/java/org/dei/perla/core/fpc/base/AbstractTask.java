package org.dei.perla.core.fpc.base;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import org.dei.perla.core.fpc.Attribute;
import org.dei.perla.core.fpc.FpcException;
import org.dei.perla.core.fpc.Task;
import org.dei.perla.core.fpc.TaskHandler;
import org.dei.perla.core.engine.Record;

/**
 * An abstract implementation of the {@link Task} interface. It is the base
 * class implemented by all {@link Task}s scheduled from an
 * {@link AbstractOperation}.
 *
 * @author Guido Rota (2014)
 *
 */
public abstract class AbstractTask implements Task {

	private AtomicBoolean running = new AtomicBoolean(true);
	private final AbstractOperation<? extends AbstractTask> op;
	private final RecordPipeline pipeline;
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
	 *            {@link Record}s to other interested users.
	 * @param pipeline
	 *            {@link RecordPipeline} used to process new {@link Record}
	 *            prior to notifying them to the {@link TaskHandler}.
	 */
	public AbstractTask(AbstractOperation<? extends AbstractTask> op,
			TaskHandler handler, RecordPipeline pipeline) {
		this.op = op;
		this.handler = handler;
		this.pipeline = pipeline == RecordPipeline.EMPTY ? null : pipeline;

		// Enrich the attribute set with all attributes added by the Pipeline
		List<Attribute> atts = new ArrayList<>(op.getAttributes());
		if (pipeline != null) {
			atts.addAll(pipeline.attributes());
		}
		this.atts = Collections.unmodifiableList(atts);
	}

	@Override
	public final List<Attribute> getAttributes() {
		return atts;
	}

	@Override
	public final boolean isRunning() {
		return running.get();
	}

	/**
	 * Returns the {@link Operation} used to schedule this {@link Task}
	 *
	 * @return {@link Operation} that scheduled this {@link Task}
	 */
	protected final Operation getOperation() {
		return op;
	}

	/**
	 * A method invoked whenever the {@code AbstractTask} is stopped. It can be
	 * overridden by concrete {@code AbstractTask} implementation to add custom
	 * shutdown behaviour.
	 */
	public void doStop() {
	}

	@Override
	public final void stop() {
		if (!running.compareAndSet(true, false)) {
			return;
		}
		doStop();
		op.remove(this);
		handler.complete(this);
	}

	/**
	 * Immediately cancels the {@link AbstractTask} execution following an error
	 * occurred to the connected {@link Operation}.
	 *
	 * This method is intended to be called by an {@link Operation} object to
	 * indicate that an exception occurred while processing a script. Invoking
	 * this method will not remove the task from the {@link AbstractOperation}'s
	 * task list.
	 *
	 * Invoking this method does not produce any effect if the
	 * {@code AbstractTask} is stopped
	 *
	 * @param cause
	 *            Cause of the error
	 */
	protected void operationError(FpcException cause) {
		if (!running.compareAndSet(true, false)) {
			return;
		}
		doStop();
		handler.error(this, cause);
	}

	/**
	 * <p>
	 * Immediately stops the {@link AbstractTask} execution.
	 * </p>
	 *
	 * <p>
	 * This method is intended to be called by an {@link AbstractOperation}
	 * object to indicate that no more records will be produced. Invoking this
	 * method will not remove the task from the {@link AbstractOperation}'s task
	 * list.
	 * </p>
	 *
	 * <p>
	 * Invoking this method does not produce any effect if the
	 * {@code AbstractTask} is stopped
	 * </p>
	 */
	protected final void operationStopped() {
		if (!running.compareAndSet(true, false)) {
			return;
		}
		doStop();
		handler.complete(this);
	}

	/**
	 * <p>
	 * Runs the a new {@link Record} in the {@link RecordPipeline} and handles
	 * it over to the registered {@link TaskHandler}. This method is intended to
	 * be invoked by a {@link AbstractOperation} whenever a new record is
	 * produced by the remote device.
	 * </p>
	 *
	 * <p>
	 * Invoking this method does not produce any effect if the
	 * {@code AbstractTask} is stopped
	 * </p>
	 *
	 * @param record
	 *            {@link Record} to be processed
	 */
	protected final void processRecord(Record record) {
		if (!running.get()) {
			return;
		}

		Record output;
		if (pipeline != null) {
			output = pipeline.run(record);
		} else {
			output = record;
		}
		handler.newRecord(this, output);
	}

	/**
	 * <p>
	 * Invokes the registered {@link TaskHandler} to inform any interested
	 * object that the {@link Task} is complete, and that no new {@link Record}
	 * are going to be produced.
	 * </p>
	 *
	 * <p>
	 * Invoking this method does not produce any effect if the
	 * {@code AbstractTask} is stopped
	 * </p>
	 */
	protected final void notifyComplete() {
		if (!running.compareAndSet(true, false)) {
			return;
		}
		op.remove(this);
		handler.complete(this);
	}

	/**
	 * Invokes the registered {@link TaskHandler} to inform any interested
	 * object that an error has occurred. The additional {@code stop} parameter
	 * may be used to indicate whether the error is unrecoverable (no new
	 * {@link Record}s will be produced) or not.
	 *
	 * Invoking this method does not produce any effect if the
	 * {@code AbstractTask} is stopped
	 *
	 * @param cause
	 *            Cause of the error
	 * @param stop
	 *            Stops the {@link AbstractTask} if set to true
	 */
	protected final void notifyError(Throwable cause, boolean stop) {
		if (!running.get()) {
			return;
		}

		if (stop && running.compareAndSet(true, false)) {
			op.remove(this);
		} else {
			return;
		}

		handler.error(this, cause);
	}

}
