package org.dei.perla.core.channel.simulator;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.dei.perla.core.fpc.descriptor.IORequestDescriptor;

@XmlRootElement(name = "request")
@XmlAccessorType(XmlAccessType.FIELD)
public class SimulatorIORequestDescriptor extends IORequestDescriptor {

	@XmlAttribute(name = "generator", required = true)
	private String generatorId;

	public String getGeneratorId() {
		return generatorId;
	}

}
