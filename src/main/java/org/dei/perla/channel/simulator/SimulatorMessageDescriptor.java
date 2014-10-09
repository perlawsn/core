package org.dei.perla.channel.simulator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;

import org.dei.perla.fpc.descriptor.FieldDescriptor;
import org.dei.perla.fpc.descriptor.MessageDescriptor;

/**
 * A custom <code>MessageDescriptor</code> implementation designed for the
 * <code>SimulatorChannel</code>.
 * 
 * Simulator messages are composed of a flat sequence of fields.
 * 
 * This class is only intended to be used in conjunction with the
 * <code>SimulatorChannel</code> and <code>SimulatorMapperFactory</code>
 * components.
 * 
 * 
 * @author Guido Rota (2014)
 * 
 */
@XmlRootElement(name = "message")
@XmlAccessorType(XmlAccessType.FIELD)
public class SimulatorMessageDescriptor extends MessageDescriptor {

	/**
	 * Flat list of fields.
	 */
	@XmlElementRef(name = "field")
	private List<SimulatorMessageFieldDescriptor> fieldList = new ArrayList<>();

	@Override
	public List<? extends FieldDescriptor> getFieldList() {
		return Collections.unmodifiableList(fieldList);
	}

}
