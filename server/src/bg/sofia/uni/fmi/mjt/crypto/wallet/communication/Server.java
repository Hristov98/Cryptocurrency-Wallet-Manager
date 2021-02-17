package bg.sofia.uni.fmi.mjt.crypto.wallet.communication;

import java.io.IOException;
import java.nio.channels.SocketChannel;

public interface Server {
    void start();

    String readClientRequest();

    String processRequest(String clientRequestJSON);

    void sendServerResponse(SocketChannel channel, String clientRequestJSON) throws IOException;

    void stop();
}
