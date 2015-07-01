package org.dei.perla.core.descriptor.instructions;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * <p>
 * Unsupported sampling period instruction. This error instruction can be
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
 * {@code <i:unsupported-period suggested="50" /> }
 * </pre>
 *
 * @author Guido Rota (2015)
 */
@XmlRootElement(name = "unsupported-period")
@XmlAccessorType(XmlAccessType.FIELD)
public class UnsupportedPeriodInstructionDescriptor extends
        InstructionDescriptor {

    @XmlAttribute(required = true)
    private String suggested;

    public UnsupportedPeriodInstructionDescriptor() {
        this.suggested = "";
    }

    public UnsupportedPeriodInstructionDescriptor(String suggested) {
        this.suggested = suggested;
    }

    public String getSuggested() {
        return suggested;
    }

}
