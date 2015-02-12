package org.dei.perla.core.fpc.descriptor.instructions;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * <p>
 * Breakpoint intstruction descriptor. Instructs the <code>FpcFactory</code> to
 * create a new <code>EmitInstruction</code>.
 * </p>
 *
 * Usage:
 * <pre>
 * {@code <i:emit /> }
 * </pre>
 *
 * @author Guido Rota (2014)
 *
 */
@XmlRootElement(name = "emit")
public class EmitInstructionDescriptor extends InstructionDescriptor {

}
