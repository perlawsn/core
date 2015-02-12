package org.dei.perla.core.descriptor.instructions;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * <p>
 * Breakpoint intstruction descriptor. Instructs the <code>FpcFactory</code> to
 * create a <code>BreakpointInstruction</code>.
 * </p>
 *
 * Usage:
 * <pre>
 * {@code <i:breakpoint /> }
 * </pre>
 *
 * @author Guido Rota (2014)
 *
 */
@XmlRootElement(name = "breakpoint")
public class BreakpointInstructionDescriptor extends InstructionDescriptor {

}
