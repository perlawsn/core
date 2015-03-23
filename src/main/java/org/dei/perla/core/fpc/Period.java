package org.dei.perla.core.fpc;

import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;

/**
 * A simple immutable container class used to represent a sampling period.
 *
 * @author Guido Rota 23/03/15.
 */
public final class Period {

    private final int value;
    private final TemporalUnit unit;

    public Period(int value, TemporalUnit unit) {
        this.value = value;
        this.unit = unit;
    }

    /**
     * Returns the value of the {@code Period}. Together with the unit of
     * measurement, this field identifies the total period duration.
     *
     * @return {@code Period} value
     */
    public int getValue() {
        return value;
    }

    /**
     * Returns the unit of measurement that identifies the magnitude the
     * period value.
     *
     * @return unit of measurement of the period value
     */
    public TemporalUnit getUnit() {
        return unit;
    }

    /**
     * Converts the {@code Period} duration in milliseconds.
     *
     * @return period duration in milliseconds
     */
    public long toMillis() {
        return unit.getDuration().toMillis() * value;
    }

}
