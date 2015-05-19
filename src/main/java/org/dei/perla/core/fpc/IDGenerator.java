package org.dei.perla.core.fpc;

/**
 * Interface for a class that dynamically generates {@link Fpc} identifiers.
 *
 * <p>Implementations of this class are employed by the {@link FpcFactory} to
 * dynamically generate new {@link Fpc} identifiers in case the Device
 * Descriptor received from the remote device does not contain a static PerLa
 * Device ID attribute.
 *
 * @author Guido Rota 18/05/15.
 */
public interface IDGenerator {

    /**
     * Generates a new {@link Fpc} identifier
     *
     * @return new {@link Fpc} identifier
     */
    public int generateID();

    /**
     * Releases a previously generated identifier. This method can be used by
     * well-behaved {@link FpcFactory} to release a previously requested
     * identifier in case the {@link Fpc} creation process cannot be
     * completed successfully.
     *
     * @param id Identifier to be released
     */
    public void releaseID(int id);

}
