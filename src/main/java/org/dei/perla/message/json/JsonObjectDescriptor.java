package org.dei.perla.message.json;

import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;

import org.dei.perla.fpc.descriptor.FieldDescriptor;
import org.dei.perla.fpc.descriptor.MessageDescriptor;

@XmlRootElement(name="object")
@XmlAccessorType(XmlAccessType.FIELD)
public class JsonObjectDescriptor extends MessageDescriptor {
	
	@XmlElementRef(name="value", required=true)
	private List<JsonValueDescriptor> valueList;
	
	public List<JsonValueDescriptor> getValueList() {
		return valueList;
	}
	
	@Override
	public List<? extends FieldDescriptor> getFieldList() {
		return Collections.unmodifiableList(valueList);
	}
	
}
