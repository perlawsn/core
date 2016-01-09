package org.dei.perla.core.engine;

import org.dei.perla.core.message.FpcMessage;
import org.dei.perla.core.utils.Check;

/**
 * Sets a field in a <code>Script</code> variable. The value being set
 * results from the evaluation of the <code>value</code> Java EL expression.
 *
 * @author Guido Rota (2014)
 *
 */
public class SetComplexInstruction extends BasicInstruction {

    private final String variable;
    private final String field;
    private final Class<?> fieldType;
    private final String value;

    public SetComplexInstruction(String variable, String field, Class<?> fieldType,
            String value) {
        this.variable = variable;
        this.field = field;
        this.fieldType = fieldType;
        this.value = value;
    }

    protected String getVariable() {
        return variable;
    }

    protected String getField() {
        return field;
    }

    protected Class<?> getFieldType() {
        return fieldType;
    }

    protected String getValue() {
        return value;
    }

    @Override
    public void runBasic(Runner runner) throws ScriptException {
        Object target = runner.ctx.getVariable(variable);
        Check.notNull(target, "Target variable '" + variable +
                "' does not exist");
        if (!(target instanceof FpcMessage)) {
            throw new ScriptException(
                    "Target variable '" + variable + "' is not an FpcMessage");
        }
        Object object = Executor.evaluateExpression(runner.ctx, value, fieldType);
        ((FpcMessage) target).setField(field, object);
    }

}
