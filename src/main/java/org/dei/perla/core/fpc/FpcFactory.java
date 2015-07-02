package org.dei.perla.core.fpc;

import org.dei.perla.core.descriptor.DeviceDescriptor;

/**
 * A software component for creating new <code>FPC</code> entities.
 *
 * @author Guido Rota (2014)
 *
 */
public interface FpcFactory {

    /**
     * Parses a <code>DeviceDescriptor</code> and creates an <code>FPC</code>
     * tailored to handle the associated device.
     *
     * The following validation steps must be performed on the
     * <code>DeviceDescriptor</code>
     * <ul>
     * <li>Device checks:</li>
     * <ul>
     * <li>Device name is set</li>
     * <li>At least one attribute is declared</li>
     * <li>At least one message is declared</li>
     * </ul>
     *
     * <li>Attribute checks:</li>
     * <ul>
     * <li>Attribute ids are set</li>
     * <li>There are no duplicate attribute ids</li>
     * <li>'value' is set for STATIC attributes</li>
     * <li>Permission is set to READ-ONLY for STATIC attributes</li>
     * <li>Timestamp attributes cannot be static</li>
     * </ul>
     *
     * <li>Message checks:</li>
     * <ul>
     * <li>Message ids are set</li>
     * <li>There are no duplicate message ids</li>
     * <li>'value' is set for STATIC-qualified fields</li>
     * <li>'attribute-id' is set on ATTRIBUTE-qualified fields and corresponds
     * to a valid device attribute</li>
     * <li>There are no ATTRIBUTE-qualified fields bound to a static attribute</li>
     * <li>The same attribute is not bound multiple time to the same message
     * field</li>
     * </ul>
     *
     * <li>Channel checks:</li>
     * <ul>
     * <li>Channel ids are set</li>
     * <li>There are no duplicate channel ids</li>
     * </ul>
     *
     * </ul>
     *
     * @param descriptor {@link Fpc} descriptor
     * @param id {@link Fpc} identifier number
     *
     * @return new {@link Fpc} object.
     * @throws FpcCreationException if an error occurs while building the
     * {@link Fpc}
     */
    public Fpc createFpc(DeviceDescriptor descriptor, int id)
            throws FpcCreationException;

}
