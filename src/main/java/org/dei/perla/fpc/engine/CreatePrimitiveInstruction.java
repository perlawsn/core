package org.dei.perla.fpc.engine;

import org.dei.perla.fpc.descriptor.DataType;

public class CreatePrimitiveInstruction extends BasicInstruction {

	private final String variable;
	private final DataType type;
	
	public CreatePrimitiveInstruction(String variable, DataType type) {
		this.variable = variable;
		this.type = type;
	}
	
	protected String getVariable() {
		return variable;
	}
	
	protected DataType getType() {
		return type;
	}
	
	@Override
	protected void runBasic(Runner runner) throws ScriptException {
		Object obj = null;
		switch (type) {
		case ID:
		case INTEGER:
			obj = new Integer(0);
			break;
		case FLOAT:
			obj =  new Float(0);
			break;
		case STRING:
			obj =  new String();
			break;
		case BOOLEAN:
			obj = new Boolean(false);
			break;
		case TIMESTAMP:
			// TODO: Implement
			throw new RuntimeException("unimplemented");
		}
		
		runner.ctx.setVariable(variable, obj);
	}

}
