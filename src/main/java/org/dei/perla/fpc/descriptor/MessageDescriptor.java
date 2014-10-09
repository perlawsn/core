package org.dei.perla.fpc.descriptor;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

/**
 * <p>
 * Java representation of a <code>Message</code> to be used by the FPCFactory
 * and related classes (MapperFactory) to create new FPCs. This class
 * contains various JAXB annotations that allow automatic data binding from an
 * XML file.
 * </p>
 * 
 * <p>
 * <code>Message</code>s are used for defining the format of data that can be
 * used to communicate with the remote device. Each <code>Message</code> must
 * contain at least one <code>FieldDescriptor</code>. Inclusion of field
 * descriptors is however message-dependent, since different types of messages
 * may follow completely different rules.
 * </p>
 * 
 * <p>
 * This abstract class has to be extended by an actual field implementation in
 * order to be used (see JsonMessageDescriptor for a concrete example).
 * </p>
 * 
 * @author Guido Rota (2014)
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class MessageDescriptor {

	/**
	 * Message identifier
	 */
	@XmlAttribute(required = true)
	private String id;

	public String getId() {
		return id;
	}

	/**
	 * Returns a List of all fields contained in this MessageDescriptor
	 * 
	 * This method is designed to iterate over the <code>FieldDescriptor</code>s
	 * defined for this message, regardless of the actual MessageDescriptor
	 * implementation. It is mainly used by the <code>FpcFactory</code> to
	 * perform sanity checks on the XML Device Descriptor.
	 * 
	 * @return List of all fields contained in this MessageDescriptor
	 */
	public abstract List<? extends FieldDescriptor> getFieldList();

}
