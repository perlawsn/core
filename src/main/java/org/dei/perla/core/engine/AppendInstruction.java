package org.dei.perla.core.engine;

import org.dei.perla.core.message.FpcMessage;

/**
 * A {@link Script} instruction for appending new elements to a list field of a
 * {@link Script} variable. The value being set results from the evaluation of
 * the <code>value</code> Java EL expression.
 *
 * @author Guido Rota (2014)
 *
 */
public class AppendInstruction extends BasicInstruction {

	private final String variable;
	private final String field;
	private final Class<?> type;
	private final String value;

	public AppendInstruction(String variable, String field, Class<?> type,
			String value) {
		this.variable = variable;
		this.field = field;
		this.type = type;
		this.value = value;
	}

	protected String getVariable() {
		return variable;
	}

	protected String getField() {
		return field;
	}

	protected String getValue() {
		return value;
	}

	@Override
	protected void runBasic(Runner runner) throws ScriptException {
		Object target = runner.ctx.getVariable(variable);
		if (!(target instanceof FpcMessage)) {
			throw new ScriptException("Target variable '" + variable
					+ "' is not an FpcMessage");
		}
		Object object = Executor.evaluateExpression(runner.ctx, value, type);
		((FpcMessage) target).appendElement(field, object);
	}

}
