package org.dei.perla.fpc.engine;

import org.dei.perla.fpc.descriptor.DataType;

public class CreatePrimitiveVarInstruction extends BasicInstruction {

	private final String name;
	private final DataType type;

	public CreatePrimitiveVarInstruction(String name, DataType type) {
		this.name = name;
		this.type = type;
	}

	protected String getVariable() {
		return name;
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
			obj = 0;
			break;
		case FLOAT:
			obj =  0.0f;
			break;
		case STRING:
			obj =  new String();
			break;
		case BOOLEAN:
			obj = false;
			break;
		case TIMESTAMP:
			// TODO: Implement
			throw new RuntimeException("unimplemented");
		}

		runner.ctx.setVariable(name, obj);
	}

}
