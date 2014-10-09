package org.dei.perla.fpc.engine;

import org.dei.perla.utils.Conditions;

/**
 * Abstract base implementation of the <code>Instruction</code> interface.
 * Contains pre-implemented methods for managing a linked list of instructions
 * 
 * @author Guido Rota (2014)
 *
 */
public abstract class BasicInstruction implements Instruction {

	private Instruction next = null;

	@Override
	public void setNext(Instruction next) throws IllegalStateException {
		if (this.next != null) {
			throw new IllegalStateException(
					"The next instruction has already been set.");
		}
		this.next = Conditions.checkNotNull(next, "next");
	}

	@Override
	public Instruction next() {
		return next;
	}

	@Override
	public final Instruction run(Runner runner) throws ScriptException {
		runBasic(runner);
		return next;
	}

	/**
	 * Method invoked by the execution engine to run an <code>Instruction</code>
	 * 
	 * @param runner
	 *            <code>Runner</code> instance containing context information
	 *            needed to execute the instruction
	 * @throws ScriptException
	 *             Thrown when the instruction cannot be completed successfully
	 */
	protected abstract void runBasic(Runner runner) throws ScriptException;

}
