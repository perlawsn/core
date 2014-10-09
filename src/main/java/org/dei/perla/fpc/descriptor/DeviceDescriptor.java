package org.dei.perla.fpc.descriptor;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Java representation of a Device, mainly to be used by the FPCFactory and
 * related classes to create new FPCs. This class
 * contains various JAXB annotations that allow automatic data binding from an
 * XML file.
 * 
 * 
 * @author Guido Rota (2014)
 * 
 */
@XmlRootElement(name = "device")
@XmlAccessorType(XmlAccessType.FIELD)
public class DeviceDescriptor {

	/**
	 * Device type
	 */
	@XmlAttribute(name = "name", required = true)
	private String name;

	/**
	 * List of attributes exposed by the device. Attributes in this list can be
	 * queried using the PerLa language.
	 */
	@XmlElement(name = "attribute", required = true)
	@XmlElementWrapper(name = "attributes")
	private List<AttributeDescriptor> attributeList = new ArrayList<>();

	/**
	 * List of messages that can be used by the FPC to exchange information with
	 * the device.
	 */
	@XmlElementWrapper(name = "messages")
	@XmlElementRef(name = "message", required = true)
	private List<MessageDescriptor> messageList = new ArrayList<>();

	/**
	 * List of channels that can be used by the device to communicate with the
	 * device.
	 */
	@XmlElementWrapper(name = "channels")
	@XmlElementRef(name = "channel", required = true)
	private List<ChannelDescriptor> channelList = new ArrayList<>();

	/**
	 * List of RequestDescriptors used for creating the <code>IORequest</code>s
	 * that will be used to retrieve data from the device.
	 */
	@XmlElementWrapper(name = "requests")
	@XmlElementRef(name = "request", required = true)
	private List<IORequestDescriptor> requestList = new ArrayList<>();

	/**
	 * List of operations supported by the FPC
	 */
	@XmlElementWrapper(name = "operations")
	@XmlElementRef(name = "operation", required = true)
	private List<OperationDescriptor> operationList = new ArrayList<>();

	public DeviceDescriptor() {
	}

	public DeviceDescriptor(String name,
			List<AttributeDescriptor> attributeList,
			List<MessageDescriptor> messageList,
			List<ChannelDescriptor> channelList,
			List<IORequestDescriptor> requestList,
			List<OperationDescriptor> operationList) {
		this.name = name;
		this.attributeList = attributeList;
		this.messageList = messageList;
		this.channelList = channelList;
		this.requestList = requestList;
		this.operationList = operationList;
	}

	public String getName() {
		return name;
	}

	public List<AttributeDescriptor> getAttributeList() {
		return attributeList;
	}

	public List<MessageDescriptor> getMessageList() {
		return messageList;
	}

	public List<ChannelDescriptor> getChannelList() {
		return channelList;
	}

	public List<IORequestDescriptor> getRequestList() {
		return requestList;
	}

	public List<OperationDescriptor> getOperationList() {
		return operationList;
	}

}
