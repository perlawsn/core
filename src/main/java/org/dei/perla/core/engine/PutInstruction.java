package org.dei.perla.core.engine;

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
    private final Class<?> type;
    private final int idx;

    public PutInstruction(String exp, Class<?> type, int idx) {
        this.exp = exp;
        this.type = type;
        this.idx = idx;
    }

    protected String getExpression() {
        return exp;
    }

    protected Class<?> getType() {
        return type;
    }

    protected int getIndex() {
        return idx;
    }

    @Override
    protected void runBasic(Runner runner) throws ScriptException {
        Object result = Executor.evaluateExpression(runner.ctx, exp, type);
        runner.ctx.putAttribute(idx, result);
    }

}
