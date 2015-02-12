package org.dei.perla.core.engine;

/**
 * A simple class for storing {@code Script} parameters. {@code ScriptParameter}
 * s can be accessed from inside a {@code Script} using the
 * {@code param['parameter_name']} EL expression.
 *
 * @author Guido Rota (2014)
 *
 */
public class ScriptParameter {

	private final String name;
	private final Object value;

	/**
	 * Creates a new {@code ScriptParameter} with the specified name and value
	 *
	 * @param name
	 *            Parameter name
	 * @param value
	 *            Parameter value
	 */
	public ScriptParameter(String name, Object value) {
		this.name = name;
		this.value = value;
	}

	/**
	 * Retrieves the parameter name.
	 *
	 * @return Parameter name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Retrieves the parameter value.
	 *
	 * @return Parameter value
	 */
	public Object getValue() {
		return value;
	}

}
