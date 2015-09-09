package org.dei.perla.core.fpc.base;

import org.dei.perla.core.engine.Script;
import org.dei.perla.core.fpc.TaskHandler;
import org.dei.perla.core.fpc.Attribute;
import org.dei.perla.core.fpc.SamplePipeline;
import org.dei.perla.core.utils.AsyncUtils;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public final class OneoffOperation extends BaseOperation<BaseTask> {

    private final Script script;

    public OneoffOperation(String id, List<Attribute> atts, Script script) {
        super(id, atts);
        this.script = script;
    }

    protected Script getScript() {
        return script;
    }

    @Override
    public BaseTask doSchedule(Map<String, Object> parameterMap,
            TaskHandler handler, SamplePipeline pipeline) {
        return new ScriptTask(this, handler, pipeline);
    }

    @Override
    protected void doStop() {}

    @Override
    protected void doStop(Consumer<Operation> handler) {
        // Invoke in new thread to preserve asynchronous locking semantics
        AsyncUtils.runInNewThread(() -> {
            handler.accept(this);
        });
    }

}
