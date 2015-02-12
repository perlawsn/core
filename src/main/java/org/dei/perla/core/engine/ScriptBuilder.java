package org.dei.perla.core.engine;

/**
 * A convenience class for creating new <code>Script</code> objects. Supports
 * method chaining through the <code>add</code> method to simplify script
 * creation.
 *
 * @author Guido Rota (2014)
 *
 */
public class ScriptBuilder {

	private Instruction start = null;
	private Instruction last = null;

	/**
	 * Initializes a new <code>ScriptBuilder</code> instance.
	 *
	 * @return New <code>ScriptBuilder</code> instance.
	 */
	public static ScriptBuilder newScript() {
		return new ScriptBuilder();
	}

	// Private constructor, new instances of the ScriptBuilder class have to be
	// created using the static newScript method.
	private ScriptBuilder() {
	}

	/**
	 * Adds a new <code>Instruction</code> to the <code>Script</code>. The
	 * <code>ScriptBuilder</code> returned by this method can be used to chain
	 * several <code>add</code> method invocations.
	 *
	 * @param instruction
	 *            <code>Instruction</code> to add.
	 * @return Current <code>ScriptBuilder</code> instance.
	 */
	public ScriptBuilder add(Instruction instruction) {
		if (start == null) {
			start = last = instruction;
		} else {
			last.setNext(instruction);
			last = instruction;
		}
		return this;
	}

	/**
	 * Creates a new <code>Script</code> using the instructions contained in the
	 * <code>ScriptBuilder</code> and the script name passed as parameter. This
	 * method adds a <code>StopInstruction</code> to the end of the script code
	 * if not already present.
	 *
	 * @param name
	 *            <code>Script</code> name
	 * @return <code>Script</code> instance
	 */
	public Script buildScript(String name) {
		if (!(last instanceof StopInstruction)) {
			this.add(new StopInstruction());
		}
		return new Script(name, start);
	}

	/**
	 * Returns the starting instruction contained in this
	 * <code>ScriptBuilder</code>.
	 *
	 * @return Script code
	 */
	public Instruction getCode() {
		return start;
	}

}
