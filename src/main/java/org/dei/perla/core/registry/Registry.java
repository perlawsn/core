package org.dei.perla.core.registry;

import org.dei.perla.core.fpc.Fpc;

import java.util.Collection;

/**
 * A catalogue for storing and indexing {@link Fpc} objects.
 *
 * @author Guido Rota
 */
public interface Registry {

    /**
     * Returns the complete list of {@link Fpc} objects contained in the
     * {@code Registry}
     *
     * @return List of all registered {@link Fpc}
     */
	public Collection<Fpc> getAll();

    /**
     * Retrieves a single {@link Fpc} by Device ID
     *
     * @param id Identifier of the {@link Fpc}
     * @return {@link Fpc} corresponding to the device with the desired
     * identifier.
     */
	public Fpc get(int id);

    /**
     * Queries the {@code Registry} to retrieve the {@link Fpc} objects with
     * the desired characteristics.
     *
     * @param with attributes that the device must posses in order to be
     *             considered for selection
     * @param without attributes that the device must not posses in order to be
     *                considered for selection
     * @return list of {@link Fpc} with the desired attributes
     */
	public Collection<Fpc> get(Collection<DataTemplate> with,
			Collection<DataTemplate> without);

    /**
     * Adds a new {@link Fpc} to the {@code Registry}.
     *
     * @param fpc {@link Fpc} to be added
     * @throws DuplicateDeviceIDException in case the Device ID of the {@link
     * Fpc} being added is already taken by another {@link Fpc} in the {@code
     * Registry}.
     */
	public void add(Fpc fpc) throws DuplicateDeviceIDException;

    /**
     * Removes an {@link Fpc} from the {@code Registry}
     *
     * @param fpc {@link Fpc} to be removed
     */
	public void remove(Fpc fpc);

}
