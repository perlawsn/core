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
@XmlRootElement(name = "periodic")
public class PeriodicOperationDescriptor extends OperationDescriptor {

	@XmlElementWrapper(name = "start", required = true)
	@XmlElementRef(name = "instruction")
	private List<InstructionDescriptor> startScript = new ArrayList<>();

	@XmlElementWrapper(name = "stop", required = true)
	@XmlElementRef(name = "instruction")
	private List<InstructionDescriptor> stopScript = new ArrayList<>();

	@XmlElementRef(name = "on")
	private List<OnReceiveDescriptor> onReceiveList = new ArrayList<>();

	public List<InstructionDescriptor> getStartScript() {
		return startScript;
	}

	public List<InstructionDescriptor> getStopScript() {
		return stopScript;
	}

	public List<OnReceiveDescriptor> getOnReceiveList() {
		return onReceiveList;
	}

}
