package org.dei.perla.core.descriptor.instructions;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * <p>
 * Unsupported sampling rate instruction. This error instruction can be
 * used to notify the the user that the selected sampling period is not
 * supported by the end device, and that the requested sampling cannot be
 * performed.
 *
 * <p>
 * The @{code suggested} attribute can be used to propose a valid alternative
 * sampling period.
 *
 * Usage:
 *
 * <pre>
 * {@code <i:unsupported-rate suggested="50" /> }
 * </pre>
 *
 * @author Guido Rota (2015)
 */
@XmlRootElement(name = "unsupported-rate")
@XmlAccessorType(XmlAccessType.FIELD)
public class UnsupportedRateInstructionDescriptor extends
        InstructionDescriptor {

    @XmlAttribute(required = true)
    private String suggested;

    public UnsupportedRateInstructionDescriptor() {
        this.suggested = "";
    }

    public UnsupportedRateInstructionDescriptor(String suggested) {
        this.suggested = suggested;
    }

    public String getSuggested() {
        return suggested;
    }

}
