package org.dei.perla.core.engine;

import org.dei.perla.core.descriptor.AttributeDescriptor;
import org.dei.perla.core.descriptor.DataType;

/**
 * Populates the current sample with the specified variable attribute. See the
 * <code>ExecutionContext</code> javadoc for more information about sample
 * management.
 *
 * @author Guido Rota (2014)
 *
 */
public class PutInstruction extends BasicInstruction {

    private final String exp;
    private final AttributeDescriptor att;
    private final int idx;

    public PutInstruction(String exp, AttributeDescriptor att, int idx) {
        this.exp = exp;
        this.att = att;
        this.idx = idx;
    }

    protected String getExpression() {
        return exp;
    }

    protected AttributeDescriptor getAttribute() {
        return att;
    }

    protected int getIndex() {
        return idx;
    }

    @Override
    protected void runBasic(Runner runner) throws ScriptException {
        Class<?> type = DataType.getClass(att.getType());
        Object result = Executor.evaluateExpression(runner.ctx, exp, type);
        runner.ctx.putAttribute(idx, result);
    }

}
