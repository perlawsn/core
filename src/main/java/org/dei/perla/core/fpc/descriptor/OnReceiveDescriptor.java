package org.dei.perla.core.fpc.descriptor;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;

import org.dei.perla.core.fpc.descriptor.instructions.InstructionDescriptor;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "on")
public class OnReceiveDescriptor {

	@XmlAttribute(required = true)
	private String message;

	@XmlAttribute(required = true)
	private String variable;

	@XmlAttribute(required = false)
	private boolean sync = false;

	@XmlElementRef(name = "instruction")
	private List<InstructionDescriptor> instructionList = new ArrayList<>();

	public String getMessage() {
		return message;
	}

	public String getVariable() {
		return variable;
	}

	public boolean isSync() {
		return sync;
	}

	public List<InstructionDescriptor> getInstructionList() {
		return instructionList;
	}

}
