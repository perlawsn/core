package org.dei.perla.core.fpc;

import java.time.Instant;

/**
 * PerLa data types
 *
 * @author Guido Rota (2015)
 */
public abstract class DataType implements Comparable<DataType> {

    public static final TypeClass ANY =
            new TypeClass("any");
    public static final TypeClass NUMERIC =
            new TypeClass("numeric");

    public static final ConcreteType ID =
            new ConcreteType("id", Integer.class);
    public static final ConcreteType INTEGER =
            new ConcreteType("integer", Integer.class);
    public static final ConcreteType FLOAT =
            new ConcreteType("float", Float.class);
    public static final ConcreteType STRING =
            new ConcreteType("string", String.class);
    public static final ConcreteType BOOLEAN =
            new ConcreteType("boolean", Boolean.class);
    public static final ConcreteType TIMESTAMP =
            new ConcreteType("timestamp", Instant.class);

    // Order sequence, employed to define a total order between data types
    private static int orderSeq = 0;

    private final String id;
    protected final Integer order;

    public DataType(String id) {
        this.order = orderSeq++;
        this.id = id;
    }

    public final String getId() {
        return id;
    }

    protected final int getOrder() {
        return order;
    }

    public abstract Class<?> getJavaClass();

    public abstract boolean match(DataType o);

    public abstract int compareMatch(DataType o);

    public final int compareTo(DataType o) {
        if (order.equals(o.order)) {
            return 0;
        } else if (order < o.order) {
            return -1;
        } else {
            return 1;
        }
    }

    public static boolean isPrimitive(String name) {
        if (name.equals(ID.getId()) ||
                name.equals(INTEGER.getId()) ||
                name.equals(FLOAT.getId()) ||
                name.equals(STRING.getId()) ||
                name.equals(BOOLEAN.getId()) ||
                name.equals(TIMESTAMP.getId())) {
            return true;
        } else {
            return false;
        }
    }


    /**
     * A concrete PerLa data type
     *
     * @author Guido Rota (2015)
     */
    public static final class ConcreteType extends DataType {

        private final Class<?> javaClass;

        private ConcreteType(String name, Class<?> javaClass) {
            super(name);
            this.javaClass = javaClass;
        }

        public Class<?> getJavaClass() {
            return javaClass;
        }

        public boolean match(DataType o) {
            if (o instanceof TypeClass) {
                return o.match(this);
            } else {
                return this == o;
            }
        }

        public int compareMatch(DataType o) {
            if (o instanceof TypeClass) {
                return -1 * o.compareMatch(this);
            } else {
                return this.order.compareTo(o.order);
            }
        }

        public static ConcreteType parse(String id) {
            switch (id.toLowerCase()) {
                case "id":
                    return DataType.ID;
                case "integer":
                    return DataType.INTEGER;
                case "float":
                    return DataType.FLOAT;
                case "string":
                    return DataType.STRING;
                case "boolean":
                    return DataType.BOOLEAN;
                case "timestamp":
                    return DataType.TIMESTAMP;
                default:
                    return null;
            }
        }

        public Object valueOf(String value) {
            return ConcreteType.valueOf(this, value);
        }

        public static Object valueOf(ConcreteType type, String value) {
            if (type == INTEGER) {
                return Integer.valueOf(value);
            } else if (type == FLOAT) {
                return Float.valueOf(value);
            } else if (type == BOOLEAN) {
                return Boolean.valueOf(value);
            } else if (type == STRING) {
                return value;
            } else if (type == ID) {
                return Integer.valueOf(value);
            } else if (type == TIMESTAMP) {
                throw new IllegalArgumentException(
                        "Cannot parse TIMESTAMP values from string. Use DateUtils instead.");
            } else {
                throw new IllegalArgumentException("Unexpected PerLa type " + type);
            }
        }

    }

    /**
     * Type class implementation
     *
     * @author Guido Rota (2015)
     */
    public static final class TypeClass extends DataType {

        private TypeClass(String name) {
            super(name);
        }

        public Class<?> getJavaClass() {
            throw new RuntimeException(
                    "No Java class available for PerLa Type Class");
        }

        public boolean match(DataType o) {
            if (this == ANY) {
                return true;
            } else if (this == NUMERIC) {
                return o == INTEGER || o == FLOAT;
            } else {
                throw new RuntimeException("Unknown TypeClass '" + this + "'");
            }
        }

        public int compareMatch(DataType o) {
            if (this == ANY || o == ANY) {
                return 0;
            } else if (this == NUMERIC) {
                if (o == INTEGER || o == FLOAT || o == NUMERIC) {
                    return 0;
                } else {
                    return -1;
                }
            } else {
                throw new RuntimeException("Unknown TypeClass '" + this + "'");
            }
        }

    }

}
