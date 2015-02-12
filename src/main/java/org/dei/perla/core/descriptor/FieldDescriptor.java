package org.dei.perla.core.descriptor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

/**
 * <p>
 * Java representation of a message Field, mainly to be used by the FPCFactory
 * and related classes to create new FPCs. This class contains various JAXB
 * annotations that allow automatic data binding from an XML file.
 * </p>
 *
 * <p>
 * This abstract class has to be extended by an actual field implementation in
 * order to be used (see JsonObjectDescriptor for a concrete example).
 * </p>
 *
 *
 * @author Guido Rota (2014)
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class FieldDescriptor {

	/**
	 * Name of the field
	 */
	@XmlAttribute(required = true)
	private String name;

	@XmlAttribute(name = "qualifier", required = false)
	private FieldQualifier qualifier;

	@XmlAttribute(required = true)
	private String type;

	/**
	 * Java value of the static field.
	 *
	 */
	@XmlAttribute(required = false)
	private String value;

	/**
	 * Additional information formatting information to be used by the message
	 * mapper for marshalling and unmarshalling the field.
	 */
	@XmlAttribute(required = false)
	private String format;

	public FieldDescriptor() {
		this.value = null;
		this.qualifier = FieldQualifier.FIELD;
		this.format = null;
	}

	public FieldDescriptor(String name, FieldQualifier qualifier, String type,
			String value, String format) {
		this.name = name;
		this.qualifier = qualifier;
		this.type = type;
		this.value = value;
		this.format = format;
	}

	public String getName() {
		return name;
	}

	public boolean isField() {
		return qualifier == FieldQualifier.FIELD;
	}

	public boolean isStatic() {
		return qualifier == FieldQualifier.STATIC;
	}

	public boolean isList() {
		return qualifier == FieldQualifier.LIST;
	}

	public String getType() {
		return type;
	}

	public String getValue() {
		return value;
	}

	public String getFormat() {
		return format;
	}

	@XmlEnum
	public enum FieldQualifier {

		@XmlEnumValue("field")
		FIELD,

		@XmlEnumValue("static")
		STATIC,

		@XmlEnumValue("list")
		LIST

	}

}
