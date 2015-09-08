package org.dei.perla.core.descriptor;

import org.dei.perla.core.fpc.DataType;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

/**
 * <p>
 * Java representation of a PerLa attribute, mainly to be used by the FPCFactory
 * and related classes to create new FPCs.
 * </p>
 *
 * <p>
 * Each device attribute describes a unit of information that can be queried or
 * set using a PerLa query. Attributes are characterized by the following
 * properties:
 * <ul>
 * <li>Identifier: name of the attribute. Attribute identifiers must be unique
 * in the scope of a single device.</li>
 * <li>Type: data type of the attribute.</li>
 * <li>Access type: indicates how the attribute is generated by the device
 * <ul>
 * <li>static: the value does not change for the entire lifespan of the remote
 * device</li>
 * <li>probing: the value is dynamically probed from the remote device every
 * time the attribute is requested.</li>
 * <li>non-probing: accesses to the attribute return a value cached in the FPC
 * (no interaction with the device is required). The value of non-probing
 * attributes is update by the PerLa framework at regular intervals.
 * </ul>
 * </li>
 * <li>Permission: access permission to the attribute
 * <ul>
 * <li>read-only: the attribute can only be read from the device</li>
 * <li>write-only: the attribute can only be set to the device</li>
 * <li>read-write: the attribute can both be read and set</li>
 * </ul>
 * </li>
 * <li>value: value of the attribute. This property can only be used to
 * characterized STATIC attributes.</li>
 * </ul>
 * </p>
 *
 * <p>
 * This class contains various JAXB annotations that allow automatic data
 * binding from an XML file.
 * </p>
 *
 *
 * @author Guido Rota (2014)
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class AttributeDescriptor {

	/**
	 * Identifier of the attribute
	 */
	@XmlAttribute(required = true)
	private String id;

	/**
	 * Attribute data type: FLOAT, INTEGER, BOOLEAN, STRING, ID, TIMESTAMP
	 */
	@XmlAttribute(required = true)
	private String type;

	/**
	 * Attribute access type: STATIC or DYNAMIC. Default value is DYNAMIC
	 */
	@XmlAttribute
	private AttributeAccessType access;

	/**
	 * Attribute permission: READ-ONLY, WRITE-ONLY, READ-WRITE (default:
	 * READ_ONLY)
	 */
	@XmlAttribute
	private AttributePermission permission;

	/**
	 * Value initializer for attributes with STATIC access type
	 */
	@XmlAttribute
	private String value;

	public AttributeDescriptor() {
		access = AttributeAccessType.DYNAMIC;
		permission = AttributePermission.READ_ONLY;
	}

	/**
	 * Creates a new dynamic attribute
	 *
	 * @param id Attribute id
	 * @param type Attribute type
	 * @param permission Attribute permission
	 */
	public AttributeDescriptor(String id, String type,
			AttributePermission permission) {
		this.id = id;
		this.type = type;
		this.access = AttributeAccessType.DYNAMIC;
		this.permission = permission;
	}

	/**
	 * Creates a new static attribute
	 *
	 * @param id Attribute id
	 * @param type Attribute type
	 * @param value Attribute value
	 */
	public AttributeDescriptor(String id, String type, String value) {
		this.id = id;
		this.type = type;
		this.access = AttributeAccessType.STATIC;
		this.permission = AttributePermission.READ_ONLY;
		this.value = value;
	}

	public String getId() {
		return id;
	}

	public String getType() {
		return type;
	}

	public AttributeAccessType getAccess() {
		return access;
	}

	public AttributePermission getPermission() {
		return permission;
	}

	public String getValue() {
		return value;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "[" + "id=" + id + ", "
				+ "type=" + type + "]";
	}

	@XmlEnum
	public enum AttributeAccessType {

		@XmlEnumValue("static")
		STATIC,

		@XmlEnumValue("dynamic")
		DYNAMIC
	}

	@XmlEnum
	public enum AttributePermission {

		@XmlEnumValue("read-only")
		READ_ONLY,

		@XmlEnumValue("write-only")
		WRITE_ONLY,

		@XmlEnumValue("read-write")
		READ_WRITE

	}

}
