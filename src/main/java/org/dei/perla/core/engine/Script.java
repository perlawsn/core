package org.dei.perla.core.engine;

import org.dei.perla.core.utils.Conditions;

/**
 * A <code>Script</code> that can be executed by the Fpc execution engine. Each
 * <code>Script</code> is identified by a name and a linked list of
 * instructions.
 *
 * @author Guido Rota (2014)
 *
 */
public class Script {

	private final String name;
	private final Instruction start;

	public Script(String name, Instruction start) {
		this.name = Conditions.checkNotNull(name, "name");
		this.start = Conditions.checkNotNull(start, "start");
	}

	/**
	 * Name of the <code>Script</code>
	 *
	 * @return Script name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the first instruction of the <code>Script</code>
	 *
	 * @return First <code>Script</code> instruction
	 */
	public Instruction getCode() {
		return start;
	}

}
