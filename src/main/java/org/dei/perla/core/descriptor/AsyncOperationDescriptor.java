package org.dei.perla.core.descriptor;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.dei.perla.core.descriptor.instructions.InstructionDescriptor;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "async")
public class AsyncOperationDescriptor extends OperationDescriptor {

	@XmlElementWrapper(required = false)
	@XmlElementRef(name = "instruction")
	private List<InstructionDescriptor> start = new ArrayList<>();

	@XmlElementRef
	private OnReceiveDescriptor on;

	public List<InstructionDescriptor> getStartScript() {
		return start;
	}

	public OnReceiveDescriptor getOnReceive() {
		return on;
	}

}
