package org.dei.perla.core.channel.simulator;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.dei.perla.core.fpc.descriptor.FieldDescriptor;

/**
 * A custom <code>FieldDescriptor</code> implementation designed for the
 * <code>SimulatorChannel</code>.
 *
 * This class does not add any field to its parent implementation, and is only
 * used to reify the abstract <code>FieldDescriptor</code>.
 *
 * This class is only intended to be used in conjunction with the
 * <code>SimulatorChannel</code> and <code>SimulatorMapperFactory</code>
 * components.
 *
 *
 * @author Guido Rota (2014)
 *
 */
@XmlRootElement(name = "field")
@XmlAccessorType(XmlAccessType.FIELD)
public class SimulatorMessageFieldDescriptor extends FieldDescriptor {
}
