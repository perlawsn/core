package org.dei.perla.core.fpc.engine;

/**
 * <p>
 * A single <code>Script</code> instruction. <code>Instruction</code>
 * implementations will usually extend the abstract
 * <code>BasicInstruction</code> class, which provides a default implementation
 * of various <code>Instruction</code> methods.
 * </p>
 *
 * <p>
 * The <code>setNext()</code> and <code>next()</code> methods allow users to set
 * and retrieve the next code block that will be executed after the current one
 * is over. It is important to note that the instruction returned or set with
 * these two methods does not necessarily corresponds with the instruction that
 * will be run immediately after the current instruction.
 * </p>
 *
 * <p>
 * For example, the <code>next()</code> method for the
 * <code>IfInstruction</code> will return the first instruction to be executed
 * after the entire if block has run, i.e. the instruction which will come after
 * either the then-clause or the else-clause is taken. This instruction, at
 * runtime, may be different from the return value of the <code>run()</code>
 * method, since this latter instruction represents what the execution engine
 * needs to run given the current runtime conditions. Continuing with the
 * previous example, the return value of the <code>IfInstruction.run()</code>
 * method is either the first instruction of the then-clause or the first
 * instruction of the else-clause, whereas the instruction of the
 * <code>IfInstruction.next()</code> method is the instruction that will be run
 * after the then or else clauses are over.
 * </p>
 *
 * @author Guido Rota (2014)
 *
 */
public interface Instruction {

	/**
	 * Sets the <code>Instruction</code> which has to be run after the current
	 * one.
	 *
	 * @param instruction
	 *            Next <code>Instruction</code> to execute.
	 * @throws IllegalStateException
	 *             When setNext() is invoked after the next instruction has
	 *             already been set
	 */
	public void setNext(Instruction instruction) throws IllegalStateException;

	/**
	 * Returns the <code>Instruction</code> that will be run after the current
	 * one.
	 *
	 * @return next <code>Instruction</code> to be run after the current one
	 */
	public Instruction next();

	/**
	 * Method invoked by the execution engine to run an <code>Instruction</code>
	 *
	 * @param runner
	 *            <code>Runner</code> instance containing context information
	 *            needed to execute the instruction
	 * @return Next <code>Instruction</code> to execute
	 * @throws ScriptException
	 *             Thrown when the instruction cannot be completed successfully
	 */
	public Instruction run(Runner runner) throws ScriptException;

	public static Instruction getLastInstruction(Instruction instruction) {
		while (instruction.next() != null) {
			instruction = instruction.next();
		}
		return instruction;
	}

}
