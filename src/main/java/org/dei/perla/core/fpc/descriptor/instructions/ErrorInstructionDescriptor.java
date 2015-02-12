package org.dei.perla.core.fpc.descriptor.instructions;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * <p>
 * Error intstruction descriptor. Instructs the <code>FpcFactory</code> to
 * create a new <code>ErrorInstruction</code>.
 * </p>
 *
 * <p>
 * The message field may be used to specify an optional textual description of
 * the error.
 * </p>
 *
 * Usage:
 *
 * <pre>
 * {@code <i:error message="error_message" /> }
 * </pre>
 *
 * @author Guido Rota (2014)
 *
 */
@XmlRootElement(name = "error")
@XmlAccessorType(XmlAccessType.FIELD)
public class ErrorInstructionDescriptor extends InstructionDescriptor {

	@XmlAttribute
	private String message;

	public ErrorInstructionDescriptor() {
		message = "";
	}

	public ErrorInstructionDescriptor(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

}
