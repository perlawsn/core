package org.dei.perla.core.descriptor;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;

import org.dei.perla.core.descriptor.instructions.InstructionDescriptor;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "get")
public class GetOperationDescriptor extends OperationDescriptor {

    @XmlElementRef(name = "instruction")
	private List<InstructionDescriptor> script = new ArrayList<>();

	public List<InstructionDescriptor> getScript() {
		return script;
	}

}
