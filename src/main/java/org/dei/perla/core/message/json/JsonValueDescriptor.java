package org.dei.perla.core.message.json;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.dei.perla.core.fpc.descriptor.FieldDescriptor;

@XmlRootElement(name = "value")
@XmlAccessorType(XmlAccessType.FIELD)
public class JsonValueDescriptor extends FieldDescriptor {

}
