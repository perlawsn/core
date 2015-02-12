package org.dei.perla.core.fpc.descriptor.instructions;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * <p>
 * If intstruction descriptor. Instructs the <code>FpcFactory</code> to create a
 * new <code>IfInstruction</code>.
 * </p>
 *
 * <p>
 * The <code>condition</code> attribute must contain a boolean EL expression,
 * whose result is used by the instruction to decide which branch should be
 * taken next. The <code>else</code> instruction block is optional and may be
 * omitted.
 * </p>
 *
 * Usage:
 *
 * <pre>
 * {@code
 * <i:if condition="error_message">
 * 	<i:then>
 * 		<!-- Then instruction block -->
 * 	</i:then>
 * 	<i:else>
 * 		<!-- Else instruction block -->
 * 	</i:else>
 * </i:if>
 * }
 * </pre>
 *
 * @author Guido Rota (2014)
 *
 */
@XmlRootElement(name = "if")
@XmlAccessorType(XmlAccessType.FIELD)
public class IfInstructionDescriptor extends InstructionDescriptor {

	@XmlAttribute
	private String condition;

	@XmlElementWrapper(name = "then", required = true)
	@XmlElementRef(name = "instruction")
	private List<InstructionDescriptor> thenInstructionList = new ArrayList<>();

	@XmlElementWrapper(name = "else", required = false)
	@XmlElementRef(name = "instruction")
	private List<InstructionDescriptor> elseInstructionList = new ArrayList<>();

	public IfInstructionDescriptor() {
	}

	public IfInstructionDescriptor(String condition,
			List<InstructionDescriptor> thenInstructionList,
			List<InstructionDescriptor> elseInstructionList) {
		this.condition = condition;
		this.thenInstructionList = thenInstructionList;
		this.elseInstructionList = elseInstructionList;
	}

	public String getCondition() {
		return condition;
	}

	public List<InstructionDescriptor> getThenInstructionList() {
		return thenInstructionList;
	}

	public List<InstructionDescriptor> getElseInstructionList() {
		return elseInstructionList;
	}

}
