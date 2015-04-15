package org.dei.perla.core.registry;

import org.dei.perla.core.sample.Attribute;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Guido Rota 15/04/15.
 */
public final class DataTemplate {

    private static final Map<String, DataTemplate> cache = new HashMap<>();

    private final String id;
    private final TypeClass typeClass;

    private DataTemplate(String id, TypeClass typeClass) {
        this.id = id;
        this.typeClass = typeClass;
    }

    public static DataTemplate create(String id, TypeClass typeClass) {
        String key = id + typeClass;
        DataTemplate dt = cache.get(key);
        if (dt == null) {
            dt = new DataTemplate(id, typeClass);
            cache.put(key, dt);
        }
        return dt;
    }

    public String getId() {
        return id;
    }

    public TypeClass getTypeClass() {
        return typeClass;
    }

    public boolean contains(Attribute a) {
        if (!a.getId().equals(id)) {
            return false;
        }

        if (!typeClass.contains(a.getType())) {
            return false;
        }

        return true;
    }

}
