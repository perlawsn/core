package org.dei.perla.core.fpc.base;

import org.dei.perla.core.engine.Script;
import org.dei.perla.core.fpc.TaskHandler;
import org.dei.perla.core.sample.Attribute;
import org.dei.perla.core.sample.SamplePipeline;
import org.dei.perla.core.utils.AsyncUtils;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public final class OneoffOperation extends AbstractOperation<AbstractTask> {

	private final Script script;

	public OneoffOperation(String id, List<Attribute> atts, Script script) {
		super(id, atts);
		this.script = script;
	}

	protected Script getScript() {
		return script;
	}

	@Override
	public AbstractTask doSchedule(Map<String, Object> parameterMap,
			TaskHandler handler, SamplePipeline pipeline) {
		ScriptTask t = new ScriptTask(this, handler, pipeline);
		// Synchronization ensures that handler invocations by the task are
		// postponed until the doSchedule() method has completed
		synchronized (t) {
			t.start();
			return t;
		}
	}

	@Override
	protected void doStop() {}

	@Override
	protected void doStop(Consumer<Operation> handler) {
		// Synchronization and AsyncUtils.runInNewThread ensure that the
		// handler is effectively asynchronously called after the doStop
		// invocation is has been completed.
		AsyncUtils.runInNewThread(() -> {
			synchronized (OneoffOperation.this) {
				handler.accept(this);
			}
		});
	}

}
