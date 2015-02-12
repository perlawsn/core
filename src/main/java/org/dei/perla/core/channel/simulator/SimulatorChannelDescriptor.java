package org.dei.perla.core.channel.simulator;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;

import org.dei.perla.core.descriptor.ChannelDescriptor;

/**
 * A <code>ChannelDescriptor</code> implementation for the
 * <code>SimulatorChannel</code>.
 *
 * This class encloses a list of <code>SimulatorResponseDescriptor</code>s, each
 * of which carries all information needed by the <code>SimulatorChannel</code>
 * for generating a <code>ChannelResponse</code>.
 *
 * See <code>SimulatorChannel</code> Javadoc for more information about the
 * corresponding XML Device Descriptor syntax.
 *
 *
 * @author Guido Rota (2014)
 *
 */
@XmlRootElement(name = "channel")
@XmlAccessorType(XmlAccessType.FIELD)
public class SimulatorChannelDescriptor extends ChannelDescriptor {

	@XmlElementRef(name = "response", required = true)
	private List<GeneratorDescriptor> responseList = new ArrayList<>();

	public List<GeneratorDescriptor> getResponseList() {
		return responseList;
	}

}
