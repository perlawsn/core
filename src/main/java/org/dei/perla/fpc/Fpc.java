package org.dei.perla.fpc;

import java.util.Collection;
import java.util.Map;

import org.dei.perla.utils.StopHandler;

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

    public Collection<Attribute> getAttributes();

    public Task set(Map<Attribute, Object> valueMap, TaskHandler handler);

    public Task get(Collection<Attribute> attributes, TaskHandler handler);

    public Task get(Collection<Attribute> attributes, long periodMs,
            TaskHandler handler);

    public Task async(Collection<Attribute> attributes, TaskHandler handler);

    public void stop(StopHandler<Fpc> handler);

}
