package org.dei.perla.core.fpc.base;

import org.dei.perla.core.engine.Script;
import org.dei.perla.core.fpc.TaskHandler;
import org.dei.perla.core.sample.Attribute;
import org.dei.perla.core.sample.SamplePipeline;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class OneoffOperation extends AbstractOperation<AbstractTask> {

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
		return new ScriptTask(this, handler, pipeline);
	}

	@Override
	protected void doStop() {
	}

	@Override
	protected void doStop(Consumer<Operation> handler) {
		handler.accept(this);
	}

}
