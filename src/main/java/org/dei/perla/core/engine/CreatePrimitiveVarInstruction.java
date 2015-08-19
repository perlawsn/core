package org.dei.perla.core.engine;

import org.dei.perla.core.fpc.DataType;

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
			obj = "";
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
