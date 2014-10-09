package org.dei.perla.fpc.descriptor.instructions;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * <p>
 * Stop intstruction descriptor. Instructs the <code>FpcFactory</code> to create
 * a <code>StopInstruction</code>.
 * </p>

 * Usage:
 * 
 * <pre>
 * {@code <i:stop /> }
 * </pre>
 * 
 * @author Guido Rota (2014)
 *
 */
@XmlRootElement(name = "stop")
public class StopInstructionDescriptor extends InstructionDescriptor {

}
