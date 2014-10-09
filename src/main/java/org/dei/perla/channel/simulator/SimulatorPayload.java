package org.dei.perla.channel.simulator;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Map;

import org.dei.perla.channel.Payload;

/**
 * A custom <code>Payload</code> implementation designed for the
 * <code>SimulatorChannel</code>.
 * 
 * This class is only intended to be used in conjunction with the
 * <code>SimulatorChannel</code> and <code>SimulatorMapper</code>
 * components. To enforce this all normal <code>Payload</code> methods throw an
 * <code>UnsupportedOperationException</code> upon invocation, preventing any
 * <code>Channel</code> implementation other than the
 * <code>SimulatorChannel</code> to access any information carried in this
 * payload.
 * 
 * @author Guido Rota (2014)
 * 
 */
public class SimulatorPayload implements Payload {

	private final Map<String, Object> valueMap;

	protected SimulatorPayload(Map<String, Object> valueMap) {
		this.valueMap = valueMap;
	}

	/**
	 * Returns a <code>Map</code> containing the the data stored inside this
	 * <code>SimulatorPayload</code>.
	 * 
	 * This method is used by the <code>SimulatorMapper</code> to
	 * retrieve the payload data during the unmarshal operation.
	 * 
	 * @return <code>Map</code> containing the payload data
	 */
	protected Map<String, Object> getValueMap() {
		return valueMap;
	}

	@Override
	public Charset getCharset() {
		throw new UnsupportedOperationException(
				"Invalid use of SimulatorPayload");
	}

	@Override
	public InputStream asInputStream() {
		throw new UnsupportedOperationException(
				"Invalid use of SimulatorPayload");
	}

	@Override
	public ByteBuffer asByteBuffer() {
		throw new UnsupportedOperationException(
				"Invalid use of SimulatorPayload");
	}

	@Override
	public String asString() {
		throw new UnsupportedOperationException(
				"Invalid use of SimulatorPayload");
	}

}
