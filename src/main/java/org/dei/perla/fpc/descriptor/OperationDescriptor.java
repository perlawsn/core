package org.dei.perla.fpc.descriptor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.FIELD)
public abstract class OperationDescriptor {

	@XmlAttribute(required = true)
	private String id;

	public String getId() {
		return id;
	}
	
}
