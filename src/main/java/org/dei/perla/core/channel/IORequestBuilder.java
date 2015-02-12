package org.dei.perla.core.channel;

import java.util.List;

/**
 * <p>
 * A component for creating specific <code>IORequest</code> objects. Instances
 * of this interface are used by the <code>Fpc</code> to create requests for
 * communicating with the remote device or service they are connected to.
 * </p>
 *
 * <p>
 * <code>IORequestBuilder</code>s are usually associated with a specific
 * <code>Channel</code> type, since every single <code>Channel</code>
 * implementation may require a different <code>IORequest</code> object.
 * </p>
 *
 * @author Guido Rota (2014)
 *
 */
public interface IORequestBuilder {

	/**
	 * Identifier of the <code>IORequest</code> created by this
	 * <code>IORequestBuilder</code>.
	 *
	 * @return request identifier
	 */
	public String getRequestId();

	/**
	 * Creates a new <code>IORequest</code> instance that can be used for
	 * requesting the execution of a new <code>Channel</code> operation.
	 *
	 * @return New <code>IORequest</code> instance
	 */
	public IORequest create();

	/**
	 * <p>
	 * Returns the list of parameters that must be set in the
	 * <code>IORequest</code> object built by this <code>IORequestBuilder</code>
	 * before submitting it to a <code>Channel</code>. Failure to set all the
	 * mandatory parameters may result in a <code>Channel</code> error.
	 * </p>
	 *
	 * @return List of parameters to set in the <code>IORequest</code> object
	 */
	public abstract List<IORequestParameter> getParameterList();

	/**
	 * <code>IORequest</code> parameter data.
	 *
	 * @author Guido Rota (2014)
	 *
	 */
	public static class IORequestParameter {

		private final String name;
		private final boolean mandatory;

		public IORequestParameter(String name, boolean mandatory) {
			this.name = name;
			this.mandatory = mandatory;
		}

		/**
		 * Parameter name
		 *
		 * @return parameter name
		 */
		public String getName() {
			return name;
		}

		/**
		 * Indicates if the parameter is mandatory or optional. Mandatory
		 * parameters are required for a correct execution of the
		 * <code>IORequest</code>; failure to set them may result in a
		 * <code>Channel</code> error.
		 *
		 * @return true if the parameter is mandatory, false otherwise.
		 */
		public boolean isMandatory() {
			return mandatory;
		}

	}

}
