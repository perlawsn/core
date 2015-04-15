package org.dei.perla.core.registry;

import org.dei.perla.core.descriptor.DataType;

/**
 * @author Guido Rota 15/04/15.
 */
public enum TypeClass {

    ID("id"),
    TIMESTAMP("timestamp"),
    BOOLEAN("boolean"),
    INTEGER("integer"),
    FLOAT("float"),
    STRING("string"),
    WILDCARD("wildcard");

    private final String name;

    private TypeClass(String name) {
        this.name = name;
    }

    public boolean contains(DataType type) {
        switch (this) {
            case ID:
                return type == DataType.ID;
            case TIMESTAMP:
                return type == DataType.TIMESTAMP;
            case BOOLEAN:
                return type == DataType.BOOLEAN;
            case INTEGER:
                return type == DataType.INTEGER;
            case FLOAT:
                return type == DataType.FLOAT;
            case STRING:
                return type == DataType.STRING;
            case WILDCARD:
                return true;
            default:
                throw new RuntimeException("Unexpected TypeClass " + this);
        }
    }

}
