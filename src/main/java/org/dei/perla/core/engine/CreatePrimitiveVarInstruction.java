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
        if (type == DataType.ID || type == DataType.INTEGER) {
			obj = 0;
		} else if (type == DataType.FLOAT) {
            obj = 0.0f;
        } else if (type == DataType.STRING) {
            obj = "";
        } else if (type == DataType.BOOLEAN) {
            obj = false;
        } else if (type == DataType.TIMESTAMP) {
            throw new RuntimeException("Timestamp variables cannot be created");
        }

        runner.ctx.setVariable(name, obj);
    }

}
