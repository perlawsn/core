package org.dei.perla.core.fpc.engine;

/**
 * Immediately terminates <code>Script</code> execution. <code>Script</code>s
 * interrupted using this instruction result in a <code>ScriptException</code>.
 *
 * @author Guido Rota (2014)
 *
 */
public class ErrorInstruction implements Instruction {

	private final String message;

	public ErrorInstruction(String message) {
		this.message = message;
	}

	protected String getMessage() {
		return message;
	}

	@Override
	public void setNext(Instruction instruction) {
	}

	@Override
	public Instruction next() {
		return null;
	}

	@Override
	public Instruction run(Runner runner) throws ScriptException {
		throw new ScriptException(message);
	}

}
