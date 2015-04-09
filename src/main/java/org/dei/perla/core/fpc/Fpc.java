package org.dei.perla.core.fpc;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.dei.perla.core.record.Attribute;
import org.dei.perla.core.utils.StopHandler;

public interface Fpc {

    /**
     * Returns the PerLa identifier of the device associated with this
     * <code>Fpc</code>.
     *
     * @return PerLa device identifier
     */
    public int getId();

    /**
     * Returns the {@code Fpc} type in textual form. This is only a descriptive
     * information.
     *
     * @return {@code Fpc} type description
     */
    public String getType();

    /**
     * Returns the list of {@link org.dei.perla.core.record.Attribute} managed
     * by this {@code Fpc}.
     *
     * @return attributes managed by the {@code Fpc}
     */
    public Collection<Attribute> getAttributes();

    /**
     * Sets the value of the attributes passed as parameter on the remote
     * device.
     *
     * @param values attribute-value map containing the parameters to be set
     *               on the remote device
     * @param handler completion handler invoked by the {@code Fpc} to notify
     *                the completion of the set operation
     * @return {@link Task} object corresponding to the set operation
     */
    public Task set(Map<Attribute, Object> values, boolean strict,
            TaskHandler handler);

    /**
     * Sets the value of the attributes passed as parameter on the remote
     * device with non-strict scheduling policy.
     *
     * @param values attribute-value map containing the parameters to be set
     *               on the remote device
     * @param strict strict scheduling policy flag. If set to true the
     *               operation will be run only if the {@code Fpc} can
     *               manage all the attributes requested by the user. If set
     *               to false, the operation will be run as long as the
     *               {@code Fpc} can manage at least one of the requested
     *               attributes.
     * @param handler completion handler invoked by the {@code Fpc} to notify
     *                the completion of the set operation
     * @return {@link Task} object corresponding to the set operation
     */
    public default Task set(Map<Attribute, Object> values,
            TaskHandler handler) {
        return set(values, false, handler);
    }

    /**
     * Performs a single-shot sampling operation.
     *
     * @param atts attributes to be sampled
     * @param strict strict scheduling policy flag. If set to true the
     *               operation will be run only if the {@code Fpc} can
     *               collect all the attributes requested by the user. If set
     *               to false, the operation will be run as long as the
     *               {@code Fpc} can collect at least one of the requested
     *               attributes.
     * @param handler completion handler used by the {@code Fpc} to notify
     *                the arrival of the data requested
     * @return {@link Task} object corresponding to the get operation
     */
    public Task get(List<Attribute> atts, boolean strict, TaskHandler handler);

    /**
     * Performs a single-shot sampling operation with non-strict scheduling
     * policy.
     *
     * @param atts attributes to be sampled
     * @param handler completion handler used by the {@code Fpc} to notify
     *                the arrival of the data requested
     * @return {@link Task} object corresponding to the get operation
     */
    public default Task get(List<Attribute> atts, TaskHandler handler) {
        return get(atts, false, handler);
    }

    /**
     * Performs a periodic sampling operation.
     *
     * @param atts attributes to be sampled
     * @param strict strict scheduling policy flag. If set to true the
     *               operation will be run only if the {@code Fpc} can
     *               collect all the attributes requested by the user. If set
     *               to false, the operation will be run as long as the
     *               {@code Fpc} can collect at least one of the requested
     *               attributes.
     * @param periodMs sampling period in milliseconds
     * @param handler completion handler used by the {@code Fpc} to notify
     *                the arrival of the data requested
     * @return {@link Task} object corresponding to the get operation
     */
    public Task get(List<Attribute> atts, boolean strict, long periodMs,
            TaskHandler handler);

    /**
     * Performs a periodic sampling operation.
     *
     * @param atts attributes to be sampled
     * @param strict strict scheduling policy flag. If set to true the
     *               operation will be run only if the {@code Fpc} can
     *               collect all the attributes requested by the user. If set
     *               to false, the operation will be run as long as the
     *               {@code Fpc} can collect at least one of the requested
     *               attributes.
     * @param period sampling period
     * @param handler completion handler used by the {@code Fpc} to notify
     *                the arrival of the data requested
     * @return {@link Task} object corresponding to the get operation
     */
    public default Task get(List<Attribute> atts, boolean strict,
            Duration period, TaskHandler handler) {
        return get(atts, strict, period.toMillis(), handler);
    }

    /**
     * Performs a periodic sampling operation with non-strict scheduling
     * policy.
     *
     * @param atts attributes to be sampled
     * @param periodMs sampling period in milliseconds
     * @param handler completion handler used by the {@code Fpc} to notify
     *                the arrival of the data requested
     * @return {@link Task} object corresponding to the get operation
     */
    public default Task get(List<Attribute> atts, long periodMs,
            TaskHandler handler) {
        return get(atts, false, periodMs, handler);
    }

    /**
     * Performs a periodic sampling operation with non-strict scheduling
     * policy.
     *
     * @param atts attributes to be sampled
     * @param period sampling period
     * @param handler completion handler used by the {@code Fpc} to notify
     *                the arrival of the data requested
     * @return {@link Task} object corresponding to the get operation
     */
    public default Task get(List<Attribute> atts, Duration period,
            TaskHandler handler) {
        return get(atts, false, period.toMillis(), handler);
    }

    /**
     * Starts an async request for a list of events.
     *
     * @param atts events
     * @param strict strict scheduling policy flag. If set to true the
     *               operation will be run only if the {@code Fpc} can
     *               collect all the attributes requested by the user. If set
     *               to false, the operation will be run as long as the
     *               {@code Fpc} can collect at least one of the requested
     *               attributes.
     * @param handler completion handler used by the {@code Fpc} to notify
     *                the occurrence of the desired events
     * @return {@link Task} object corresponding to the async operation
     */
    public Task async(List<Attribute> atts, boolean strict,
            TaskHandler handler);

    /**
     * Starts an async request for a list of events with non-strict scheduling
     * policy.
     *
     * @param atts events
     * @param handler completion handler used by the {@code Fpc} to notify
     *                the occurrence of the desired events
     * @return {@link Task} object corresponding to the async operation
     */
    public default Task async(List<Attribute> atts, TaskHandler handler) {
        return async(atts, false, handler);
    }

    /**
     * Stops the {@code Fpc} and all the ongoing operations.
     *
     * @param handler completion handler invoked to notify the effective
     *                termination of the {@code Fpc}
     */
    public void stop(StopHandler<Fpc> handler);

}
