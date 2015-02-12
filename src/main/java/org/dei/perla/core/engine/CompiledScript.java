package org.dei.perla.core.engine;

import java.util.Set;

import org.dei.perla.core.fpc.Attribute;

/**
 * A container class for storing compiled {@link Script}s, alongside with
 * additional information regarding input and output attributes.
 *
 * @author Guido Rota (2014)
 *
 */
public class CompiledScript {

	private final Script script;
	private final Set<Attribute> emitSet;
	private final Set<Attribute> setSet;

	protected CompiledScript(Script script, Set<Attribute> emitSet,
			Set<Attribute> setSet) {
		this.script = script;
		this.emitSet = emitSet;
		this.setSet = setSet;
	}

	/**
	 * Returns the compiled {@link Script}
	 *
	 * @return Compiled {@link Script}
	 */
	public Script getScript() {
		return script;
	}

	/**
	 * Returns the set of attributes emitted by the {@link Script} (output
	 * attributes)
	 *
	 * @return Set of output attributes
	 */
	public Set<Attribute> getEmitSet() {
		return emitSet;
	}

	/**
	 * Returns the set of attributes used as input by the {@link Script} (input
	 * attributes)
	 *
	 * @return Set of input attributes
	 */
	public Set<Attribute> getSetSet() {
		return setSet;
	}

}
