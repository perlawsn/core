package org.dei.perla.channel.simulator;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlRootElement;

import org.dei.perla.fpc.descriptor.DataType;

/**
 * This class contains all information needed by the <code>SimulatorChannel</code>
 * to dynamically generate field values.
 *
 * See <code>SimulatorChannel</code>'s javadoc entries for more information
 * about the meaning of the min and max field for different
 * <code>AttributeType</code>s.
 *
 *
 * @author Guido Rota (2014)
 *
 */
@XmlRootElement(name = "field")
@XmlAccessorType(XmlAccessType.FIELD)
public class GeneratorFieldDescriptor {

	@XmlAttribute
	private String name;

	@XmlAttribute
	private GeneratorFieldStrategy strategy;

	@XmlAttribute
	private DataType type;

	@XmlAttribute
	private String value;

	@XmlAttribute
	private Integer min;

	@XmlAttribute
	private Integer max;

    @XmlAttribute
    private String increment;

	protected GeneratorFieldDescriptor() {
		super();
		type = DataType.STRING;
		min = 0;
		max = 1;
        increment = "1";
	}

	public String getName() {
		return name;
	}

	public GeneratorFieldStrategy getStrategy() {
		return strategy;
	}

	public DataType getType() {
		return type;
	}

	public String getValue() {
		return value;
	}

	public Integer getMin() {
		return min;
	}

	public Integer getMax() {
		return max;
	}

    public String getIncrement() {
        return increment;
    }

	@XmlEnum
	public enum GeneratorFieldStrategy {

		@XmlEnumValue("static")
		STATIC,

		@XmlEnumValue("dynamic")
		DYNAMIC,

        @XmlEnumValue("step")
        STEP

	}

}
