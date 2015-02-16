package org.dei.perla.core.engine;

import java.util.Collections;

/**
 * A convenience class for creating new <code>Script</code> objects. Supports
 * method chaining through the <code>add</code> method to simplify script
 * creation.
 *
 * @author Guido Rota (2014)
 *
 */
public class ScriptBuilder {

	private Instruction first = null;
	private Instruction last = null;

	public static ScriptBuilder newScript() {
		return new ScriptBuilder();
	}

	// Private constructor, new instances of the ScriptBuilder class have to be
	// created using the static newScript method.
	private ScriptBuilder() {
	}

	public ScriptBuilder add(Instruction instruction) {
		if (first == null) {
			first = last = instruction;
		} else {
			last.setNext(instruction);
			last = instruction;
		}
		return this;
	}

	public Script buildScript(String name) {
		if (!(last instanceof StopInstruction)) {
			this.add(new StopInstruction());
		}
		return new Script(name, first, Collections.emptyList(),
                Collections.emptyList());
	}

    public Instruction getCode() {
        return first;
    }

}
