package org.dei.perla.message.urlencoded;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;

import org.dei.perla.fpc.descriptor.MessageDescriptor;

@XmlRootElement(name = "message")
@XmlAccessorType(XmlAccessType.FIELD)
public class UrlEncodedMessageDescriptor extends MessageDescriptor {

	@XmlElementRef(name = "parameter")
	private List<UrlEncodedParameter> parameterList = new ArrayList<>();

	public List<UrlEncodedParameter> getParameterList() {
		return parameterList;
	}

	@Override
	public List<UrlEncodedParameter> getFieldList() {
		return Collections.unmodifiableList(parameterList);
	}

}
