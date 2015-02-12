package org.dei.perla.core.channel.http;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.dei.perla.core.fpc.descriptor.ChannelDescriptor;

@XmlRootElement(name = "channel")
@XmlAccessorType(XmlAccessType.FIELD)
public class HttpChannelDescriptor extends ChannelDescriptor {

}
