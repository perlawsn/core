package org.dei.perla.fpc.descriptor.instructions;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "param")
public class ParameterBinding {

	@XmlAttribute
	private String variable;
	
	@XmlAttribute
	private String name;
	
	public String getVariable() {
		return variable;
	}
	
	public String getName() {
		return name;
	}
	
}
