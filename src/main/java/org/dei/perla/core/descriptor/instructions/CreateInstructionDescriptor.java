package org.dei.perla.core.descriptor.instructions;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * <p>
 * Create instruction descriptor. Instructs the <code>FpcFactory</code> to
 * create a <code>CreateInstruction</code>.
 * </p>
 *
 * <p>
 * Script variables are identified by a name, which is used to refer to
 * the variable in other <code>Script</code> instructions, and a message type
 * used by the system to infer which attributes can be set or retrieved on the
 * variable.
 * </p>
 *
 * Usage:
 *
 * <pre>
 * {@code
 * <i:create variable="variable_name" message="message_type" />
 * }
 * </pre>
 *
 * @author Guido Rota (2014)
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "var")
public class CreateInstructionDescriptor extends InstructionDescriptor {

	@XmlAttribute(required = true)
	private String name;

	@XmlAttribute(required = true)
	private String type;

	public CreateInstructionDescriptor() {
	}

	public CreateInstructionDescriptor(String name, String type) {
		this.name = name;
		this.type = type;
	}

	/**
	 * Returns the name of the variable
	 *
	 * @return Variable name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the variable type
	 *
	 * @return Variable type
	 */
	public String getType() {
		return type;
	}

}
