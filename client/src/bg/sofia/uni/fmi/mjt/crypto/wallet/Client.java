package bg.sofia.uni.fmi.mjt.crypto.wallet;

import bg.sofia.uni.fmi.mjt.crypto.wallet.communication.Request;
import bg.sofia.uni.fmi.mjt.crypto.wallet.communication.Response;

import java.io.IOException;

public interface Client {
    void start();

    void sendClientRequest(Request clientRequest) throws IOException;

    Response getServerResponse() throws IOException;

    void stop();
}
