package org.dei.perla.core.engine;

/**
 * Indicates that the device doesn't support the sampling period chosen by
 * the user. The closest supported sampling period can be retrieved through
 * the {@code getSuggested()} method
 *
 * @author Guido Rota 30/06/15.
 */
public class UnsupportedPeriodException extends ScriptException {

    private static final long serialVersionUID = 7789669365841542040L;

    private final long unsupported;
    private final long suggested;

    public UnsupportedPeriodException(long unsupported, long suggested) {
        super("Unsupported sampling period " + unsupported + ". " +
                "Closest suggested period is " + suggested + ".");
        this.unsupported = unsupported;
        this.suggested = suggested;
    }

    /**
     * Returns the unsupported sampling period chosen by the user.
     *
     * @return unsupported sampling period chosen by the user
     */
    public long getUnsupported() {
        return unsupported;
    }

    /**
     * Returns the closest supported sampling period that can be used as an
     * alternative to the unsupported sampling period chosen by the user.
     *
     * @return suggested sampling period supported by the device
     */
    public long getSuggested() {
        return suggested;
    }

}
