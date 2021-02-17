package bg.sofia.uni.fmi.mjt.crypto.wallet;

import bg.sofia.uni.fmi.mjt.crypto.wallet.communication.Request;
import bg.sofia.uni.fmi.mjt.crypto.wallet.communication.Response;
import bg.sofia.uni.fmi.mjt.crypto.wallet.exception.ClientCommunicationException;
import bg.sofia.uni.fmi.mjt.crypto.wallet.exception.ClientSetupException;
import bg.sofia.uni.fmi.mjt.crypto.wallet.exception.ClientShutdownException;
import com.google.gson.Gson;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class CryptoWalletClient implements Client {
    private static final String CLIENT_SETUP_ERROR = "An occurred while opening the client socket";
    private static final String CLIENT_CONNECTION_ERROR = "An error occurred while communicating with the server";
    private static final String CLIENT_SHUTDOWN_ERROR = "An error occurred while closing the client socket";
    private static final String HOST_NAME = "localhost";
    private static final int BUFFER_SIZE = 8192;

    private final int serverPort;
    private final ByteBuffer buffer;
    private final Scanner scanner;
    private final Gson gson;

    private SocketChannel socketChannel;
    private boolean isLoggedIn;
    private String username;
    private boolean sessionIsActive;

    public CryptoWalletClient(int port) {
        serverPort = port;
        scanner = new Scanner(System.in);
        buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);
        gson = new Gson();

        isLoggedIn = false;
        username = "guest";
        setUpClient();
    }

    private void setUpClient() {
        try {
            socketChannel = SocketChannel.open();
            socketChannel.connect(new InetSocketAddress(HOST_NAME, serverPort));
            sessionIsActive = true;
        } catch (IOException exception) {
            throw new ClientSetupException(CLIENT_SETUP_ERROR, exception);
        }
    }

    @Override
    public void start() {
        displayGreeting();
        try {
            while (sessionIsActive) {
                displayUsername();
                displayCommandMenu();

                Request clientRequest = generateClientRequest();
                String requestResult = processClientRequest(clientRequest);
                displayRequestResult(requestResult);
            }
        } catch (IOException exception) {
            throw new ClientCommunicationException(CLIENT_CONNECTION_ERROR, exception);
        }
    }

    private void displayGreeting() {
        System.out.println("Welcome to Cryptocurrency Wallet Manager.");
    }

    private void displayUsername() {
        if (isLoggedIn) {
            System.out.printf("You are currently logged in as %s.%n", username);
        } else {
            System.out.println("You are currently a guest.");
        }
    }

    private void displayCommandMenu() {
        if (isLoggedIn) {
            displayAvailableUserCommands();
        } else {
            displayAvailableGuestCommands();
        }
        System.out.print("=> ");
    }

    private void displayAvailableUserCommands() {
        System.out.print("""
                Available commands:
                1) deposit-money [amount_in_USD]
                2) list-offerings
                3) buy [offering_code] [amount_to_pay]
                4) sell [offering_code]
                5) get-wallet-summary
                6) get-wallet-overall-summary
                7) logout
                8) help [command_name]
                9) quit
                10*) save-users (Admin command for manual testing)
                """);
    }

    private void displayAvailableGuestCommands() {
        System.out.print("""
                Available commands:
                1) register [username] [password]
                2) login [username] [password]
                3) help [command_name]
                4) quit
                """);
    }

    private Request generateClientRequest() {
        String clientCommand = scanner.nextLine() + System.lineSeparator();
        return new Request(username, clientCommand);
    }

    public String processClientRequest(Request clientRequest) throws IOException {
        if (isLoggedIn) {
            return processUserRequest(clientRequest);
        } else {
            return processGuestRequest(clientRequest);
        }
    }

    private String processUserRequest(Request clientRequest) throws IOException {
        String commandWithArguments = clientRequest.getCommand();
        String mainCommand = commandWithArguments.split("\\s+")[0];

        switch (mainCommand) {
            case "logout" -> {
                return attemptLogout(clientRequest);
            }
            case "help" -> {
                return HelpSection.getInformation(commandWithArguments);
            }
            case "quit" -> {
                return disconnectFromServer(clientRequest);
            }
            default -> {
                Response serverResponse = getResultFromServer(clientRequest);
                return serverResponse.getResultMessage();
            }
        }
    }

    private String attemptLogout(Request clientRequest) throws IOException {
        Response serverResponse = getResultFromServer(clientRequest);

        if (serverResponse.isSuccessful()) {
            isLoggedIn = false;
            username = "guest";
        }

        return serverResponse.getResultMessage();
    }

    private Response getResultFromServer(Request clientRequest) throws IOException {
        sendClientRequest(clientRequest);
        return getServerResponse();
    }

    @Override
    public void sendClientRequest(Request clientRequest) throws IOException {
        String requestJSON = getRequestJSON(clientRequest);
        writeToServer(requestJSON);
    }

    private String getRequestJSON(Request request) {
        return gson.toJson(request);
    }

    private void writeToServer(String clientRequest) throws IOException {
        buffer.clear();
        buffer.put(clientRequest.getBytes());
        buffer.flip();
        socketChannel.write(buffer);
    }

    @Override
    public Response getServerResponse() throws IOException {
        String responseJSON = readResponse();
        return getResponseFromJSON(responseJSON);
    }

    private String readResponse() throws IOException {
        buffer.clear();
        socketChannel.read(buffer);
        buffer.flip();

        byte[] byteArray = new byte[buffer.remaining()];
        buffer.get(byteArray);
        return new String(byteArray, StandardCharsets.UTF_8);
    }

    private Response getResponseFromJSON(String responseJSON) {
        return gson.fromJson(responseJSON, Response.class);
    }

    private String disconnectFromServer(Request clientRequest) throws IOException {
        Response serverResponse = getResultFromServer(clientRequest);

        if (serverResponse.isSuccessful()) {
            sessionIsActive = false;
        }

        return serverResponse.getResultMessage();
    }

    private String processGuestRequest(Request clientRequest) throws IOException {
        String commandWithArguments = clientRequest.getCommand();
        String mainCommand = commandWithArguments.split("\\s+")[0];

        switch (mainCommand) {
            case "login", "register" -> {
                return attemptLogIn(clientRequest);
            }
            case "help" -> {
                return HelpSection.getInformation(commandWithArguments);
            }
            case "quit" -> {
                return disconnectFromServer(clientRequest);
            }
            default -> {
                Response serverResponse = getResultFromServer(clientRequest);
                return serverResponse.getResultMessage();
            }
        }
    }

    private String attemptLogIn(Request clientRequest) throws IOException {
        Response serverResponse = getResultFromServer(clientRequest);

        if (serverResponse.isSuccessful()) {
            isLoggedIn = true;
            username = serverResponse.getRecipient();
        }

        return serverResponse.getResultMessage();
    }

    private void displayRequestResult(String requestResult) {
        System.out.println(requestResult);
    }

    @Override
    public void stop() {
        try {
            socketChannel.close();
        } catch (IOException exception) {
            throw new ClientShutdownException(CLIENT_SHUTDOWN_ERROR, exception);
        }
    }

    public static void main(String[] args) {
        CryptoWalletClient client = new CryptoWalletClient(7676);
        client.start();
        client.stop();
    }
}
