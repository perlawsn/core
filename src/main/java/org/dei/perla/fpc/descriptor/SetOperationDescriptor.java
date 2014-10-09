package org.dei.perla.fpc.descriptor;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;

import org.dei.perla.fpc.descriptor.instructions.InstructionDescriptor;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "set")
public class SetOperationDescriptor extends OperationDescriptor {

	@XmlElementRef(name = "instruction")
	private List<InstructionDescriptor> instructionList = new ArrayList<>();

	public List<InstructionDescriptor> getInstructionList() {
		return instructionList;
	}

}
