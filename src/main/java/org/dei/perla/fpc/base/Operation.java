package org.dei.perla.fpc.base;

import java.util.Collection;
import java.util.Map;

import org.dei.perla.fpc.Attribute;
import org.dei.perla.fpc.Fpc;
import org.dei.perla.fpc.Task;
import org.dei.perla.fpc.TaskHandler;
import org.dei.perla.utils.StopHandler;

/**
 * <p>
 * An interface for accessing a single specific functionality exposed by the
 * {@link Fpc}. A single {@code Operation} may be used to serve several data
 * consumers concurrently.
 * </p>
 * 
 * @author Guido Rota (2014)
 *
 */
public interface Operation {

	/**
	 * Returns the identifier of this {@code Operation}
	 * 
	 * @return {@code Operation} identifier
	 */
	public String getId();

	/**
	 * Returns the set of device {@link Attribute}s that can be sampled or set
	 * using this operation
	 * 
	 * @return Set of {@link Attribute}s associated with the {@code Operation}
	 */
	public Collection<Attribute> getAttributes();

	/**
	 * <p>
	 * Schedules a new {@code Operation} instance. This method returns a
	 * {@link Task} object that may be used to control the execution of this
	 * {@code Operation} instance.
	 * </p>
	 *
	 * <p>
	 * {@link Task} objects are independent of each other: stopping a
	 * {@link Task} only interrupts the single data stream associated with the
	 * object being stopped. All other {@link Task}s associated with the same
	 * {@code Operation} will continue to run normally.
	 * </p>
	 * 
	 * @param parameterMap
	 *            Parameters to be passed
	 * @param handler
	 *            {@link TaskHandler} object used to asynchronously collect the
	 *            {@code Operation} output
	 * @return {@link Task} object for controlling the {@code Operation}
	 *         execution
	 * @throws IllegalArgumentException
	 *             When the parameters required to run this operation are
	 *             notfound in the parameterMap
	 * @throws IllegalStateException
	 *             If the {@code schedule} method is invoked when the
	 *             {@code Operation} is not running
	 */
	public default AbstractTask schedule(Map<String, Object> parameterMap,
			TaskHandler handler) throws IllegalArgumentException,
			IllegalStateException {
		return schedule(parameterMap, handler, null);
	}

	/**
	 * <p>
	 * Schedules a new {@code Operation} instance. This method returns a
	 * {@link Task} object that the invoking entity may use to control the
	 * execution of this {@code Operation} instance.
	 * </p>
	 *
	 * <p>
	 * {@link Task} objects are independent of each other: stopping a
	 * {@link Task} only interrupts the single data stream associated with the
	 * object being stopped. All other {@link Task}s associated with the same
	 * {@code Operation} will continue to run normally.
	 * </p>
	 * 
	 * <p>
	 * This version of the {@code schedule()} method accepts a
	 * {@link RecordPipeline} parameter that will be used to modify the raw
	 * records produced by remote device.
	 * </p>
	 * 
	 * @param parameterMap
	 *            Parameters to be passed
	 * @param handler
	 *            {@link TaskHandler} object used to asynchronously collect the
	 *            {@code Operation} output
	 * @param pipeline
	 *            {@link RecordPipeline} object used to modify the records
	 *            produced by this operation
	 * @return {@link Task} object for controlling the {@code Operation}
	 *         execution
	 * @throws IllegalArgumentException
	 *             When the parameters required to run this operation are
	 *             notfound in the parameterMap
	 * @throws IllegalStateException
	 *             If the {@code schedule} method is invoked when the
	 *             {@code Operation} is not running
	 */
	public AbstractTask schedule(Map<String, Object> parameterMap,
			TaskHandler handler, RecordPipeline pipeline)
			throws IllegalArgumentException, IllegalStateException;

	/**
	 * Indicates if this {@code Operation} can be used to schedule new
	 * {@link Task}s or not. An {@code Operation} may become unschedulable if it
	 * has been stopped or as a consequence of an unrecoverable error
	 * 
	 * @return true if new {@link Task}s can be scheduled from this
	 *         {@code Operation}, false otherwise
	 */
	public boolean isSchedulable();
	
	/**
	 * Permanently stops the {@code Operation}
	 */
	public void stop(StopHandler<Operation> handler);

}
