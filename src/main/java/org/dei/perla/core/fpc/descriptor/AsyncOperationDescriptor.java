package org.dei.perla.core.fpc.descriptor;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.dei.perla.core.fpc.descriptor.instructions.InstructionDescriptor;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "async")
public class AsyncOperationDescriptor extends OperationDescriptor {

	@XmlElementWrapper(name = "start", required = false)
	@XmlElementRef(name = "instruction")
	private List<InstructionDescriptor> startScript = new ArrayList<>();

	@XmlElementRef(name = "on")
	private OnReceiveDescriptor onReceive;

	public List<InstructionDescriptor> getStartScript() {
		return startScript;
	}

	public OnReceiveDescriptor getOnReceive() {
		return onReceive;
	}

}
