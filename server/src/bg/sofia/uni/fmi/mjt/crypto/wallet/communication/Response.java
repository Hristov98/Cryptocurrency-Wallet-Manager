package bg.sofia.uni.fmi.mjt.crypto.wallet.communication;

public class Response {
    private boolean isSuccessful;
    private String recipient;
    private String resultMessage;

    public Response(boolean isSuccessful, String recipient, String resultMessage) {
        this.isSuccessful = isSuccessful;
        this.recipient = recipient;
        this.resultMessage = resultMessage;
    }

    public boolean isSuccessful() {
        return isSuccessful;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getResultMessage() {
        return resultMessage;
    }
}
