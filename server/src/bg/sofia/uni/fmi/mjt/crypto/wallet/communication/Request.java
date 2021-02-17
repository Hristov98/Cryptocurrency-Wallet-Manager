package bg.sofia.uni.fmi.mjt.crypto.wallet.communication;

public class Request {
    private String sender;
    private String command;

    public Request(String sender, String command) {
        this.sender = sender;
        this.command = command;
    }

    public String getSender() {
        return sender;
    }

    public String getCommand() {
        return command;
    }

    public boolean isFromGuest() {
        return sender.equals("guest");
    }
}
