package org.dei.perla.core.fpc;

import java.util.Collection;
import java.util.Map;

import org.dei.perla.core.engine.Attribute;
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
     * Returns the list of {@link org.dei.perla.core.engine.Attribute} managed by this {@code Fpc}
     *
     * @return {@link org.dei.perla.core.engine.Attribute} managed by the {@code Fpc}
     */
    public Collection<Attribute> getAttributes();

    public Task set(Map<Attribute, Object> values, TaskHandler handler);

    public Task get(Collection<Attribute> atts, TaskHandler handler);

    public Task get(Collection<Attribute> attributes, long periodMs,
            TaskHandler handler);

    public Task async(Collection<Attribute> atts, TaskHandler handler);

    public void stop(StopHandler<Fpc> handler);

}
