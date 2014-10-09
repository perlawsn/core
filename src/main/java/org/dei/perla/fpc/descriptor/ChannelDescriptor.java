package org.dei.perla.fpc.descriptor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

/**
 * <p>
 * Java representation of a PerLa <code>Channel</code> to be used by the
 * FPCFactory and related classes (ChannelFactory) to create new FPCs.
 * This abstract class has to be extended by an actual field implementation in
 * order to be used (see HttpChannel and SimulatorChannel for usage examples).
 * </p>
 * 
 * <p>
 * This class contains various JAXB annotations that allow automatic data
 * binding from an XML file.
 * </p>
 * 
 * 
 * @author Guido Rota (2014)
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class ChannelDescriptor {

	/**
	 * Channel identifier. This field is used by other
	 * <code>DeviceDescriptor</code> elements to refer to this channel.
	 */
	@XmlAttribute(required = true)
	private String id;

	public ChannelDescriptor() {
	}
	
	public ChannelDescriptor(String id) {
		this.id = id;
	}
	
	public String getId() {
		return id;
	}

}
