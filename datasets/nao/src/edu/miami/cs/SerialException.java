package edu.miami.cs;

/**
 * Author: Sam Abeyruwan
 */
public class SerialException extends Exception {
    public SerialException() {
        super();
    }

    public SerialException(String message) {
        super(message);
    }

    public SerialException(String message, Throwable cause) {
        super(message, cause);
    }

    public SerialException(Throwable cause) {
        super(cause);
    }
}
