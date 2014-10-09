package org.dei.perla.fpc.descriptor.instructions;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "foreach")
@XmlAccessorType(XmlAccessType.FIELD)
public class ForeachInstructionDescriptor extends InstructionDescriptor {

	@XmlAttribute(name = "items-var", required = true)
	private String itemsVar;

	@XmlAttribute(name = "items-field", required = true)
	private String itemsField;

	@XmlAttribute(required = true)
	private String variable;

	@XmlAttribute(required = false)
	private String index;

	@XmlElementRef(name = "instruction")
	private List<InstructionDescriptor> body;

	public ForeachInstructionDescriptor() {
	}

	public ForeachInstructionDescriptor(String itemsVar, String itemsField,
			String variable, List<InstructionDescriptor> body) {
		this(itemsVar, itemsField, variable, null, body);
	}

	public ForeachInstructionDescriptor(String itemsVar, String itemsField,
			String variable, String index, List<InstructionDescriptor> body) {
		this.itemsVar = itemsVar;
		this.itemsField = itemsField;
		this.variable = variable;
		this.index = index;
		this.body = body;
	}

	public String getItemsVar() {
		return itemsVar;
	}

	public String getItemsField() {
		return itemsField;
	}

	public String getVariable() {
		return variable;
	}

	public String getIndex() {
		return index;
	}

	public List<InstructionDescriptor> getBody() {
		return body;
	}

}
