package org.dei.perla.fpc.base;

import java.util.Map;
import java.util.Set;

import org.dei.perla.fpc.Attribute;
import org.dei.perla.fpc.TaskHandler;
import org.dei.perla.fpc.engine.Script;
import org.dei.perla.utils.StopHandler;

public class OneoffOperation extends AbstractOperation<AbstractTask> {

	private final Script script;

	public OneoffOperation(String id, Set<Attribute> attributeSet, Script script) {
		super(id, attributeSet);
		this.script = script;
	}

	protected Script getScript() {
		return script;
	}

	@Override
	public AbstractTask doSchedule(Map<String, Object> parameterMap,
			TaskHandler handler, RecordPipeline pipeline) {
		return new ScriptTask(this, handler, pipeline);
	}

	@Override
	protected void doStop() {
	}

	@Override
	protected void doStop(StopHandler<Operation> handler) {
		handler.hasStopped(this);
	}

}
