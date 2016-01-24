package org.dei.perla.core.utils;

import org.dei.perla.core.fpc.Attribute;

import java.util.List;

/**
 * Shared {@link Attribute} utilities
 *
 * @author Guido Rota 24/01/16.
 */
public final class AttributeUtils {

    /**
     * Returns the index of the first occurrence of the {@link Attribute}
     * with the specified identifier
     *
     * @param list list of {@link Attribute}s
     * @param id {@link Attribute} id to search
     * @return index of the first occurrence of the {@link Attribute} with the
     * specified identifier, -1 if not found.
     */
    public static int indexOf(List<Attribute> list, String id) {
        Check.notNull(list);
        Check.notNull(id);

        for (int i = 0; i < list.size(); i++) {
            Attribute a = list.get(i);
            if (a.getId().equals(id)) {
                return i;
            }
        }
        return -1;
    }

}
