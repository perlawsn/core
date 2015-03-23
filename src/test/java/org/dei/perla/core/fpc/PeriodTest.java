package org.dei.perla.core.fpc;

import org.junit.Test;

import java.time.temporal.ChronoUnit;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.*;

/**
 * @author Guido Rota 23/03/15.
 */
public class PeriodTest {

    @Test
    public void testPeriod() {
        Period p;

        p = new Period(5, ChronoUnit.SECONDS);
        assertThat(p.getValue(), equalTo(5));
        assertThat(p.getUnit(), equalTo(ChronoUnit.SECONDS));
        assertThat(p.toMillis(), equalTo((long) 5000));

        p = new Period(12, ChronoUnit.DAYS);
        assertThat(p.getValue(), equalTo(12));
        assertThat(p.getUnit(), equalTo(ChronoUnit.DAYS));
        assertThat(p.toMillis(), equalTo((long) 12 * 24 * 60 * 60 * 1000));
    }

}
