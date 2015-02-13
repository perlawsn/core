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
@XmlRootElement(name = "periodic")
public class PeriodicOperationDescriptor extends OperationDescriptor {

	@XmlElementWrapper(required = true)
	@XmlElementRef(name = "instruction")
	private List<InstructionDescriptor> start = new ArrayList<>();

	@XmlElementWrapper(required = true)
	@XmlElementRef(name = "instruction")
	private List<InstructionDescriptor> stop = new ArrayList<>();

	@XmlElementRef
	private List<OnReceiveDescriptor> on = new ArrayList<>();

	public List<InstructionDescriptor> getStartScript() {
		return start;
	}

	public List<InstructionDescriptor> getStopScript() {
		return stop;
	}

	public List<OnReceiveDescriptor> getOnReceiveList() {
		return on;
	}

}
