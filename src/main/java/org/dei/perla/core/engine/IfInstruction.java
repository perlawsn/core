package org.dei.perla.core.engine;

/**
 * <code>Script</code> implementation of the if instruction. Each
 * <code>IfInstruction</code> is composed of a mandatory then code block, an
 * optional else code block and an instruction to be run after the whole
 * statement is done, regardless of which branch is taken.
 *
 * @author Guido Rota (2014)
 *
 */
public class IfInstruction implements Instruction {

	private final String condition;
	private final Instruction thenBlock;
	private final Instruction elseBlock;
	private Instruction afterBlock = null;

	public IfInstruction(String condition, Instruction thenInst) {
		this(condition, thenInst, null);
	}

	public IfInstruction(String condition, Instruction thenBlock,
			Instruction elseBlock) {
		this.condition = condition;
		this.thenBlock = thenBlock;
		this.elseBlock = elseBlock;
	}

	protected Instruction getThenBlock() {
		return thenBlock;
	}

	protected Instruction getElseBlock() {
		return elseBlock;
	}

	@Override
	public void setNext(Instruction next) throws IllegalStateException {
		if (afterBlock != null) {
			throw new IllegalStateException(
					"The next instruction has already been set.");
		}
		afterBlock = next;
		Instruction.getLastInstruction(thenBlock).setNext(next);
		if (elseBlock != null) {
			Instruction.getLastInstruction(elseBlock).setNext(next);
		}
	}

	@Override
	public Instruction next() {
		return afterBlock;
	}

	@Override
	public Instruction run(Runner runner) throws ScriptException {
		if (Executor.evaluateExpression(runner.ctx, condition, Boolean.class)) {
			return thenBlock;
		} else if (elseBlock != null) {
			return elseBlock;
		} else {
			return afterBlock;
		}
	}

}
