package org.dei.perla.core.fpc.engine;

/**
 * Stops the execution of the current <code>Script</code>. No next instruction
 * is allowed after a <code>StopInstruction</code>.
 *
 * @author Guido Rota (2014)
 *
 */
public class StopInstruction implements Instruction {

	@Override
	public Instruction next() {
		return null;
	}

	@Override
	public void setNext(Instruction instruction) {
		throw new UnsupportedOperationException(
				"Stop does not allow a next instruction to be set");
	}

	@Override
	public Instruction run(Runner runner) throws ScriptException {
		runner.stop();
		return null;
	}

}
