package bg.sofia.uni.fmi.mjt.crypto.wallet.exception;

public class ClientShutdownException extends RuntimeException {
    public ClientShutdownException(String message, Throwable cause) {
        super(message, cause);
    }
}
