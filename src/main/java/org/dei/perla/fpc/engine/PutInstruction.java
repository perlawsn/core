package org.dei.perla.fpc.engine;

import org.dei.perla.fpc.descriptor.AttributeDescriptor;
import org.dei.perla.fpc.descriptor.DataType;

/**
 * Populates the current record with the specified variable attribute. See the
 * <code>ExecutionContext</code> javadoc for more information about record
 * management.
 * 
 * @author Guido Rota (2014)
 *
 */
public class PutInstruction extends BasicInstruction {

	private final String expression;
	private final AttributeDescriptor attribute;

	public PutInstruction(String expression, AttributeDescriptor attribute) {
		this.expression = expression;
		this.attribute = attribute;
	}
	
	protected String getExpression() {
		return expression;
	}
	
	protected AttributeDescriptor getAttribute() {
		return attribute;
	}

	@Override
	protected void runBasic(Runner runner) throws ScriptException {
		Class<?> type = DataType.getClass(attribute.getType());
		Object result = Executor.evaluateExpression(runner.ctx, expression, type);
		runner.ctx.putAttribute(attribute.getId(), result);
	}

}
