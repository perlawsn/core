package org.dei.perla.message.json;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.dei.perla.fpc.descriptor.FieldDescriptor;

@XmlRootElement(name = "value")
@XmlAccessorType(XmlAccessType.FIELD)
public class JsonValueDescriptor extends FieldDescriptor {
	
}
