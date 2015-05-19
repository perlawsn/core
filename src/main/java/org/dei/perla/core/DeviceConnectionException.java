package org.dei.perla.core;

/**
 * Exception thrown to indicate that an error has occurred while
 * while connecting a new device with the PerLa System
 *
 * @author Guido Rota 18/05/15.
 */
public class DeviceConnectionException extends Exception {

    private static final long serialVersionUID = 5198478382322134827L;

    public DeviceConnectionException() { }

    public DeviceConnectionException(String msg) {
        super(msg);
    }

    public DeviceConnectionException(Exception e) {
        super(e);
    }

    public DeviceConnectionException(String msg, Exception e) {
        super(msg, e);
    }

}
