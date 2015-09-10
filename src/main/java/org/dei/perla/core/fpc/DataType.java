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
    protected final Integer ordinal;

    public DataType(String id) {
        this.ordinal = orderSeq++;
        this.id = id;
    }

    /**
     * Returns the type identifier
     *
     * @return String type identifier
     */
    public final String getId() {
        return id;
    }

    /**
     * Returns true if the type is concrete, false if it's a {@link TypeClass}
     *
     * @return true if the type is concrete, false if it's a {@link TypeClass}
     */
    public abstract boolean isConcrete();

    /**
     * Returns true if the type is a {@link TypeClass}, false if it's concrete
     *
     * @return true if the type is a {@link TypeClass}, false if it's concrete
     */
    public final boolean isTypeClass() {
        return !isConcrete();
    }

    /**
     * Returns the Java class associated with the PerLa {@code DataType}.
     * This information is used by the middleware to determine which object
     * type has to be instantiated in order to store a value of a well
     * defined data type.
     *
     * <p> Invocation of this method only defined for concrete data types
     * (ID, INTEGER, FLOAT, STRING, BOOLEAN, TIMESTAMP). Invocation of
     * this method on {@link TypeClass} objects will throw an exception.
     *
     * @return Java class associated with the type
     * @throws RuntimeException if the method is invoked on a {@link TypeClass}
     */
    public abstract Class<?> getJavaClass();

    /**
     * Finds the strictest of the two {@link DataType}s passed as parameter.
     * Returns {@code null} if the two types are not compatible.
     *
     * <p> Both types have to be compatible: i.e., strictest(ANY, NUMERIC)
     * returns NUMERIC, strictest(NUMERIC, BOOLEAN) returns {@code null}. This
     * method returns {@code null} if both types are concrete, as by
     * definitions two concrete types are always incompatible.
     *
     * @param t1 first type
     * @param t2 second type
     * @return the strictest of the two types, null if the types are not
     * compatible
     */
    public static final DataType strictest(DataType t1, DataType t2) {
        if (t1 == t2) {
            return t1;
        }

        if (t1.ordinal > t2.ordinal) {
            DataType tmp = t1;
            t1 = t2;
            t2 = tmp;
        }

        if (t1 == DataType.ANY) {
            return t2;
        } else if (t1 == DataType.NUMERIC &&
                (t2 == DataType.INTEGER || t2 == DataType.FLOAT)) {
            return t2;
        } else {
            return null;
        }
    }

    /**
     * Matches the current type with another data type. Match is different
     * from equalTo, as it correctly identifies {@link TypeClass}es and acts
     * accordingly, as it positively matches INTEGER and FLOAT with NUMERIC,
     * and all concrete types to ANY.
     *
     * @param o type to match the current instance with
     * @return true if the two types are compatible, false otherwise
     */
    public abstract boolean match(DataType o);

    /**
     * Similar to {@code match}, provides an additional ordering between types.
     *
     * <p> Returns 0 whenever {@code match} return true, otherwise returns
     * the relative order between types (types order follows their
     * declaration order in this class).
     *
     * @param o
     * @return 0 if the types are equal or match, < 0 if the current object is
     * lower than the other, > 0 otherwise.
     */
    public abstract int compareMatch(DataType o);

    public final int compareTo(DataType o) {
        if (ordinal.equals(o.ordinal)) {
            return 0;
        } else if (ordinal < o.ordinal) {
            return -1;
        } else {
            return 1;
        }
    }

    @Override
    public String toString() {
        return id;
    }

    /**
     * Indicates if the string passed as parameter corresponds to the id of
     * one of the concrete PerLa data types.
     *
     * @param name String to check
     * @return true if the String corresponds to the identifier of one of the
     * concrete PerLa types, false otherwise.
     */
    public static final boolean isPrimitive(String name) {
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

        public boolean isConcrete() {
            return true;
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
                return this.ordinal.compareTo(o.ordinal);
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

        public boolean isConcrete() {
            return false;
        }

        public boolean match(DataType o) {
            if (this == ANY || o == ANY) {
                return true;
            } else if (this == NUMERIC) {
                return o == INTEGER || o == FLOAT || o == NUMERIC;
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
