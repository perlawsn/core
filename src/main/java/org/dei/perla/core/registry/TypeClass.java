package org.dei.perla.core.registry;

import org.dei.perla.core.descriptor.DataType;

/**
 * @author Guido Rota 15/04/15.
 */
public enum TypeClass {

    // The order of the TypeClass enum constants is crucial for the correct
    // execution of the Registry matching methods. All wildcards must come
    // before the actual data types, and must be ordered from the most
    // general to the most specific. Actual data types order must be
    // consistent with the ordering found in the DataType enum

    // Wildcards
    ANY("any"),
    NUMERIC("numeric"),

    // Actual types
    FLOAT("float"),
    INTEGER("integer"),
    BOOLEAN("boolean"),
    STRING("string"),
    ID("id"),
    TIMESTAMP("timestamp");

    private final String name;

    private TypeClass(String name) {
        this.name = name;
    }

    public boolean match(DataType type) {
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
            case ANY:
                return true;
            case NUMERIC:
                return type == DataType.INTEGER || type == DataType.FLOAT;
            default:
                throw new RuntimeException("Unexpected TypeClass " + this);
        }
    }

    public int compareMatch(DataType type) {
        if (this == ANY) {
            return 0;
        } else if (this == NUMERIC) {
            if (type == DataType.INTEGER || type == DataType.FLOAT) {
                return 0;
            } else {
                return 1;
            }
        }

        TypeClass o = typeToClass(type);
        return this.compareTo(o);
    }

    private static TypeClass typeToClass(DataType type) {
        switch (type) {
            case FLOAT:
                return TypeClass.FLOAT;
            case INTEGER:
                return TypeClass.INTEGER;
            case BOOLEAN:
                return TypeClass.BOOLEAN;
            case STRING:
                return TypeClass.STRING;
            case ID:
                return TypeClass.ID;
            case TIMESTAMP:
                return TypeClass.TIMESTAMP;
            default:
                throw new RuntimeException("Unexpected DataType " + type);
        }
    }

    @Override
    public String toString() {
        return name;
    }

}
