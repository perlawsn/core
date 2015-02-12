package org.dei.perla.core.engine;

/**
 * Sets the value of a primitive <code>Script</code> variable. The value being
 * set results from the evaluation of the <code>value</code> Java EL expression.
 *
 * @author Guido Rota (2014)
 *
 */
public class SetPrimitiveInstruction extends BasicInstruction {

	private final String variable;
	private final Class<?> type;
	private final String value;

	public SetPrimitiveInstruction(String variable, Class<?> type, String value) {
		this.variable = variable;
		this.type = type;
		this.value = value;
	}

	protected String getVariable() {
		return variable;
	}

	protected Class<?> getType() {
		return type;
	}

	protected String getValue() {
		return value;
	}

	@Override
	protected void runBasic(Runner runner) throws ScriptException {
		Object object = Executor.evaluateExpression(runner.ctx, value, type);
		runner.ctx.setVariable(variable, object);
	}

}
