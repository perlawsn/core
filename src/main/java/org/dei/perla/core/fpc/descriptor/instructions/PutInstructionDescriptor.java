package org.dei.perla.core.fpc.descriptor.instructions;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * <p>
 * Put intstruction descriptor. Instructs the <code>FpcFactory</code> to create
 * a <code>PutInstruction</code>.
 * </p>
 *
 * Usage:
 *
 * <pre>
 * {@code <i:put value="value_expression" attribute="attribute_name" /> }
 * </pre>
 *
 * @author Guido Rota (2014)
 *
 */
@XmlRootElement(name = "put")
@XmlAccessorType(XmlAccessType.FIELD)
public class PutInstructionDescriptor extends InstructionDescriptor {

	@XmlAttribute
	private String expression;

	@XmlAttribute
	private String attribute;

	public PutInstructionDescriptor() {
	}

	public PutInstructionDescriptor(String expression, String attribute) {
		this.expression = expression;
		this.attribute = attribute;
	}

	public String getExpression() {
		return expression;
	}

	public String getAttribute() {
		return attribute;
	}

}
