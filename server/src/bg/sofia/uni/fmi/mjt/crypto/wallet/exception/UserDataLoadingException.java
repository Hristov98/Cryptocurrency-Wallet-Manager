package bg.sofia.uni.fmi.mjt.crypto.wallet.exception;

public class UserDataLoadingException extends RuntimeException {
    public UserDataLoadingException(String message) {
        super(message);
    }

    public UserDataLoadingException(String message, Throwable cause) {
        super(message, cause);
    }
}
