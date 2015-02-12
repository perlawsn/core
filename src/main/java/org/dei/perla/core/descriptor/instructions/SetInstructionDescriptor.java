package org.dei.perla.core.descriptor.instructions;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * <p>
 * Set intstruction descriptor. Instructs the <code>FpcFactory</code> to create
 * a <code>SetInstruction</code>.
 * </p>
 *
 * <p>
 * The <code>value</code> attribute may contain a static value or an EL
 * expression.
 * </p>
 *
 * Usage:
 *
 * <pre>
 * {@code <i:set variable="variable_name" attribute="attribute_name" value="value_expression"/> }
 * </pre>
 *
 * @author Guido Rota (2014)
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "set")
public class SetInstructionDescriptor extends InstructionDescriptor {

	@XmlAttribute(required = true)
	private String variable;

	@XmlAttribute(required = false)
	private String field;

	@XmlAttribute(required = true)
	private String value;

	public SetInstructionDescriptor() {
	}

	public SetInstructionDescriptor(String variable, String field,
			String value) {
		this.variable = variable;
		this.field = field;
		this.value = value;
	}

	public SetInstructionDescriptor(String variable, String value) {
		this.variable = variable;
		this.value = value;
	}

	public String getVariable() {
		return variable;
	}

	public String getField() {
		return field;
	}

	public String getValue() {
		return value;
	}

}
