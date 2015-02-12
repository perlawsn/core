package org.dei.perla.core.descriptor.instructions;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * <p>
 * Submit intstruction descriptor. Instructs the <code>FpcFactory</code> to
 * create a <code>SubmitInstruction</code>.
 * </p>
 *
 * <p>
 * The <code>param</code> elements can be used to populate a parameter of the
 * request with the contents of a <code>Script</code> variable.
 * </p>
 *
 * Usage:
 *
 * <pre>
 * {@code
 * <i:submit request="request_name" channel="channel_name" variable="output_variable_name"
 * 		message="output_variable_type">
 * 	<i:param name="parameter_name" variable="variable_name />
 * </i:submit>
 * }
 * </pre>
 *
 * @author Guido Rota (2014)
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "submit")
public class SubmitInstructionDescriptor extends InstructionDescriptor {

	@XmlAttribute
	private String request;

	@XmlAttribute
	private String channel;

	@XmlAttribute
	private String variable;

	@XmlAttribute
	private String type;

	@XmlElementRef
	private List<ParameterBinding> parameterList = new ArrayList<>();

	public SubmitInstructionDescriptor() {
	}

	public SubmitInstructionDescriptor(String request, String channel,
			String variable, String type) {
		this.request = request;
		this.channel = channel;
		this.variable = variable;
		this.type = type;
	}

	public String getRequest() {
		return request;
	}

	public String getChannel() {
		return channel;
	}

	public String getVariable() {
		return variable;
	}

	public String getType() {
		return type;
	}

	public List<ParameterBinding> getParameterList() {
		return parameterList;
	}

}
