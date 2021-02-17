package bg.sofia.uni.fmi.mjt.crypto.wallet.communication;

import bg.sofia.uni.fmi.mjt.crypto.wallet.exception.ServerCommunicationException;
import bg.sofia.uni.fmi.mjt.crypto.wallet.exception.ServerSetupException;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;

public class CryptoWalletServer implements AutoCloseable, Server {
    private static final int BUFFER_SIZE = 8192;
    private static final String HOST_NAME = "localhost";
    private static final String SERVER_SETUP_ERROR = "An error occurred while setting up the server";
    private static final String SERVER_CONNECTION_ERROR =
            "An error occurred while the server was processing client requests";

    private final int serverPort;
    private final ByteBuffer buffer;
    private final ClientRequestExecutor clientRequestExecutor;

    private ServerSocketChannel serverSocketChannel;
    private Selector selector;
    private boolean serverIsActive;

    public CryptoWalletServer(int port) {
        buffer = ByteBuffer.allocate(BUFFER_SIZE);
        serverPort = port;
        clientRequestExecutor = new ClientRequestExecutor();
    }

    @Override
    public void start() {
        setUpServer();
        while (serverIsActive) {
            try {
                int readyChannels = selector.select();
                if (readyChannels == 0) {
                    continue;
                }

                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                iterateKeys(selectedKeys);
            } catch (IOException exception) {
                throw new ServerCommunicationException(SERVER_CONNECTION_ERROR, exception);
            }
        }
    }

    private void setUpServer() {
        try {
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.bind(new InetSocketAddress(HOST_NAME, serverPort));
            serverSocketChannel.configureBlocking(false);

            selector = Selector.open();
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        } catch (IOException exception) {
            throw new ServerSetupException(SERVER_SETUP_ERROR, exception);
        }

        serverIsActive = true;
    }

    private void iterateKeys(Set<SelectionKey> selectedKeys) throws IOException {
        Iterator<SelectionKey> keyIterator = selectedKeys.iterator();
        while (keyIterator.hasNext()) {
            SelectionKey key = keyIterator.next();

            if (key.isReadable()) {
                SocketChannel channel = (SocketChannel) key.channel();

                buffer.clear();
                int readResult = channel.read(buffer);
                if (readResult <= 0) {
                    channel.close();
                    break;
                }

                String clientRequestJSON = readClientRequest();
                String serverResponseJSON = processRequest(clientRequestJSON);
                sendServerResponse(channel, serverResponseJSON);
            } else if (key.isAcceptable()) {
                acceptConnection(key);
            }

            keyIterator.remove();
        }
    }

    @Override
    public String readClientRequest() {
        buffer.flip();
        byte[] byteArray = new byte[buffer.remaining()];
        buffer.get(byteArray);

        return new String(byteArray, 0, buffer.position(), StandardCharsets.UTF_8);
    }

    @Override
    public String processRequest(String clientRequestJSON) {
        return clientRequestExecutor.processRequest(clientRequestJSON);
    }

    @Override
    public void sendServerResponse(SocketChannel channel, String clientRequestJSON) throws IOException {
        buffer.clear();
        buffer.put(clientRequestJSON.getBytes());
        buffer.flip();
        channel.write(buffer);
    }

    private void acceptConnection(SelectionKey key) throws IOException {
        ServerSocketChannel socketChannel = (ServerSocketChannel) key.channel();
        SocketChannel connection = socketChannel.accept();
        connection.configureBlocking(false);
        connection.register(selector, SelectionKey.OP_READ);
    }

    @Override
    public void stop() {
        serverIsActive = false;
    }

    @Override
    public void close() throws Exception {
        //clientRequestExecutor.saveUsersToFile();
        serverSocketChannel.close();
        selector.close();
    }

    public static void main(String[] args) {
        try (CryptoWalletServer server = new CryptoWalletServer(7676)) {
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
