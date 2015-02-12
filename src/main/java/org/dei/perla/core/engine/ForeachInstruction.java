package org.dei.perla.core.engine;

import java.util.List;

import org.dei.perla.core.engine.ExecutionContext.InstructionLocal;
import org.dei.perla.core.message.FpcMessage;

public class ForeachInstruction implements Instruction {

	private final String itemsVar;
	private final String itemsField;
	private final String variable;
	private final String index;
	private final Instruction body;

	private Instruction afterLoop = null;

	private final InstructionLocal<Integer> idx;
	private final InstructionLocal<List<?>> list;

	public ForeachInstruction(String itemsVar, String itemsField, String variable, Instruction body) {
		this(itemsVar, itemsField, variable, null, body);
	}

	public ForeachInstruction(String itemsVar, String itemsField, String variable,
			String index, Instruction body) {
		this.itemsVar = itemsVar;
		this.itemsField = itemsField;
		this.variable = variable;
		this.index = index;
		this.body = body;

		idx = new InstructionLocal<>(0);
		list = new InstructionLocal<>(null);
	}

	@Override
	public void setNext(Instruction instruction) throws IllegalStateException {
		if (afterLoop != null) {
			throw new IllegalStateException(
					"The next instruction has already been set.");
		}
		this.afterLoop = instruction;
		Instruction.getLastInstruction(body).setNext(this);
	}

	@Override
	public Instruction next() {
		return afterLoop;
	}

	@Override
	public Instruction run(Runner runner) throws ScriptException {
		List<?> l = list.getValue(runner);
		int i = idx.getValue(runner);

		// Init variables if this is the first iteration
		if (l == null) {
			idx.setValue(runner, 0);
			FpcMessage var = (FpcMessage) runner.ctx.getVariable(itemsVar);
			l = (List<?>) var.getField(itemsField);
			list.setValue(runner, l);
		}

		if (i >= l.size()) {
			return afterLoop;
		}
		runner.ctx.setVariable(variable, l.get(i));
		if (index != null) {
			runner.ctx.setVariable(index, i);
		}
		idx.setValue(runner, i + 1);
		return body;
	}

}
