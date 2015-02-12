package org.dei.perla.core.fpc.descriptor.instructions;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "append")
public class AppendInstructionDescriptor extends InstructionDescriptor {

	@XmlAttribute
	private String variable;

	@XmlAttribute
	private String field;

	@XmlAttribute
	private String value;

	public AppendInstructionDescriptor() { }

	public AppendInstructionDescriptor(String variable, String field, String value) {
		this.variable = variable;
		this.field = field;
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
