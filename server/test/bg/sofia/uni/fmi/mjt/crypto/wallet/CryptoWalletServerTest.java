package bg.sofia.uni.fmi.mjt.crypto.wallet;

import bg.sofia.uni.fmi.mjt.crypto.wallet.communication.ClientRequestExecutor;
import bg.sofia.uni.fmi.mjt.crypto.wallet.communication.CryptoWalletServer;
import bg.sofia.uni.fmi.mjt.crypto.wallet.communication.Request;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class CryptoWalletServerTest {
    private static final String WRONG_MESSAGE_ERROR = "The response is incorrect or formatted incorrectly";
    private static final int SERVER_PORT = 7777;
    private static Thread serverThread;
    private static CryptoWalletServer cryptoServer;

    @BeforeClass
    public static void startServer() {
        serverThread = new Thread(() -> {
            try (CryptoWalletServer server = new CryptoWalletServer(SERVER_PORT)) {
                cryptoServer = server;
                cryptoServer.start();
            } catch (Exception e) {
                System.out.println("An error has occurred while starting the server");
                e.printStackTrace();
            }
        });

        serverThread.start();
    }

    @Test
    public void testUnknownCommand() {
        List<Request> requests = new ArrayList<>();
        requests.add(new Request("guest", "unknown"));

        List<String> responses = processRequests(requests);
        assertNotNull(responses);

        for (String response : responses) {
            String expected = String.format("You have entered an unknown command.%n");
            assertEquals(WRONG_MESSAGE_ERROR, expected, response);
        }
    }

    @Test
    public void testRegisterWithWrongNumberOfArguments() {
        List<Request> requests = new ArrayList<>();
        requests.add(new Request("guest", "register"));
        requests.add(new Request("guest", "register test"));
        requests.add(new Request("guest", "register test test test"));

        List<String> responses = processRequests(requests);
        assertNotNull(responses);

        for (String response : responses) {
            String expected = String.format("You have entered an unknown command.%n");
            assertEquals(WRONG_MESSAGE_ERROR, expected, response);
        }
    }

    @Test
    public void testRegisterWithInvalidUsername() {
        Request request = new Request("guest", "register #$#@#$ test");

        String response = processRequest(request);
        String expected = String.format("This username contains illegal characters, please enter a valid one.%n");
        assertNotNull(response);
        assertEquals(WRONG_MESSAGE_ERROR, expected, response);
    }

    @Test
    public void testRegisterWithTakenUsername() {
        List<Request> requests = new ArrayList<>();
        requests.add(new Request("guest", "register taken test"));
        requests.add(new Request("taken", "logout"));
        requests.add(new Request("guest", "register taken test2"));

        List<String> responses = processRequests(requests);
        assertNotNull(responses);

        String expected = String.format("This username is already taken, please enter a valid one.%n");
        assertEquals(WRONG_MESSAGE_ERROR, expected, responses.get(2));
    }

    @Test
    public void testRegisterWithValidData() {
        Request request = new Request("guest", "register admin admin");

        String response = processRequest(request);
        assertNotNull(response);

        String expected = String.format("User admin has successfully been registered.%n");
        assertEquals(WRONG_MESSAGE_ERROR, expected, response);
    }

    @Test
    public void testLoginWithWrongNumberOfArguments() {
        List<Request> requests = new ArrayList<>();
        requests.add(new Request("guest", "login"));
        requests.add(new Request("guest", "login test"));
        requests.add(new Request("guest", "login test test test"));

        List<String> responses = processRequests(requests);
        assertNotNull(responses);

        for (String response : responses) {
            String expected = String.format("You have entered an unknown command.%n");
            assertEquals(WRONG_MESSAGE_ERROR, expected, response);
        }
    }

    @Test
    public void testLoginWithInvalidCredentials() {
        List<Request> requests = new ArrayList<>();
        requests.add(new Request("guest", "register valid valid"));
        requests.add(new Request("valid", "logout"));
        requests.add(new Request("guest", "login valid invalid"));
        requests.add(new Request("guest", "login invalid valid"));

        List<String> responses = processRequests(requests);
        assertNotNull(responses);

        String expected = String.format("The username or password you have entered is incorrect.%n");
        assertEquals(WRONG_MESSAGE_ERROR, expected, responses.get(2));
        assertEquals(WRONG_MESSAGE_ERROR, expected, responses.get(3));
    }

    @Test
    public void testLoginWithValidData() {
        List<Request> requests = new ArrayList<>();
        requests.add(new Request("guest", "register logged test"));
        requests.add(new Request("logged", "logout"));
        requests.add(new Request("guest", "login logged test"));

        List<String> responses = processRequests(requests);
        assertNotNull(responses);

        String expected = String.format("You have successfully logged in. Welcome back, logged.%n");
        assertEquals(WRONG_MESSAGE_ERROR, expected, responses.get(2));
    }

    @Test
    public void testDepositMoneyWithWrongNumberOfArguments() {
        List<Request> requests = new ArrayList<>();
        requests.add(new Request("guest", "register depositUnknown test"));
        requests.add(new Request("depositUnknown", "deposit-money"));
        requests.add(new Request("depositUnknown", "deposit-money test test"));

        List<String> responses = processRequests(requests);
        assertNotNull(responses);

        String expected = String.format("You have entered an unknown command.%n");
        assertEquals(WRONG_MESSAGE_ERROR, expected, responses.get(1));
        assertEquals(WRONG_MESSAGE_ERROR, expected, responses.get(2));
    }

    @Test
    public void testDepositMoneyWithInvalidNumber() {
        List<Request> requests = new ArrayList<>();
        requests.add(new Request("guest", "register depositInvalid test"));
        requests.add(new Request("depositInvalid", "deposit-money 12#@$44"));

        List<String> responses = processRequests(requests);
        assertNotNull(responses);

        String expected = String.format("Deposit cancelled. The deposit amount must be a valid number.%n");
        assertEquals(WRONG_MESSAGE_ERROR, expected, responses.get(1));
    }

    @Test
    public void testDepositMoneyWithNegativeNumber() {
        List<Request> requests = new ArrayList<>();
        requests.add(new Request("guest", "register depositNegative test"));
        requests.add(new Request("depositNegative", "deposit-money -1"));

        List<String> responses = processRequests(requests);
        assertNotNull(responses);

        String expected = String.format("Deposit cancelled. The deposit amount must be a positive number.%n");
        assertEquals(WRONG_MESSAGE_ERROR, expected, responses.get(1));
    }

    @Test
    public void testDepositMoneyWithValidNumber() {
        List<Request> requests = new ArrayList<>();
        requests.add(new Request("guest", "register depositValid test"));
        requests.add(new Request("depositValid", "deposit-money 10000"));
        requests.add(new Request("depositValid", "deposit-money 15000"));

        List<String> responses = processRequests(requests);
        assertNotNull(responses);

        String expectedFirstDeposit = String.format("Deposit successful. Current balance: $10000,0000.%n");
        assertEquals(WRONG_MESSAGE_ERROR, expectedFirstDeposit, responses.get(1));
        String expectedSecondDeposit = String.format("Deposit successful. Current balance: $25000,0000.%n");
        assertEquals(WRONG_MESSAGE_ERROR, expectedSecondDeposit, responses.get(2));
    }

    @Test
    public void testBuyWithWrongNumberOfArguments() {
        List<Request> requests = new ArrayList<>();
        requests.add(new Request("guest", "register buyUnknown test"));
        requests.add(new Request("buyUnknown", "buy"));
        requests.add(new Request("buyUnknown", "buy test"));
        requests.add(new Request("buyUnknown", "buy test test test"));

        List<String> responses = processRequests(requests);
        assertNotNull(responses);

        String expected = String.format("You have entered an unknown command.%n");
        assertEquals(WRONG_MESSAGE_ERROR, expected, responses.get(1));
        assertEquals(WRONG_MESSAGE_ERROR, expected, responses.get(2));
        assertEquals(WRONG_MESSAGE_ERROR, expected, responses.get(3));
    }

    @Ignore
    public void testBuyWithInvalidCurrency() {
        List<Request> requests = new ArrayList<>();
        requests.add(new Request("guest", "register buyCurrency test"));
        requests.add(new Request("buyCurrency", "buy aaaaaaaa 100"));

        List<String> responses = processRequests(requests);
        assertNotNull(responses);

        String expected = String.format("The currency you have entered could not be found. Please enter again.%n");
        assertEquals(WRONG_MESSAGE_ERROR, expected, responses.get(1));
    }

    @Test
    public void testBuyWithInvalidNumber() {
        List<Request> requests = new ArrayList<>();
        requests.add(new Request("guest", "register buyInvalid test"));
        requests.add(new Request("buyInvalid", "buy test 12#@$44"));

        List<String> responses = processRequests(requests);
        assertNotNull(responses);

        String expected = String.format("Purchase cancelled. The purchase amount must be a valid number.%n");
        assertEquals(WRONG_MESSAGE_ERROR, expected, responses.get(1));
    }

    @Test
    public void testBuyWithNegativeNumber() {
        List<Request> requests = new ArrayList<>();
        requests.add(new Request("guest", "register buyNegative test"));
        requests.add(new Request("buyNegative", "buy test -1"));

        List<String> responses = processRequests(requests);
        assertNotNull(responses);

        String expected = String.format("Purchase cancelled. The purchase amount must be a positive number.%n");
        assertEquals(WRONG_MESSAGE_ERROR, expected, responses.get(1));
    }

    @Test
    public void testSellWithWrongNumberOfArguments() {
        List<Request> requests = new ArrayList<>();
        requests.add(new Request("guest", "register sellUnknown test"));
        requests.add(new Request("sellUnknown", "sell"));
        requests.add(new Request("sellUnknown", "sell test test"));

        List<String> responses = processRequests(requests);
        assertNotNull(responses);

        String expected = String.format("You have entered an unknown command.%n");
        assertEquals(WRONG_MESSAGE_ERROR, expected, responses.get(1));
        assertEquals(WRONG_MESSAGE_ERROR, expected, responses.get(2));
    }

    @Ignore
    public void testSellWithInvalidCurrency() {
        List<Request> requests = new ArrayList<>();
        requests.add(new Request("guest", "register sellCurrency test"));
        requests.add(new Request("sellCurrency", "sell aaaaaaaa"));

        List<String> responses = processRequests(requests);
        assertNotNull(responses);

        String expected = String.format("The currency you have entered could not be found. Please enter again.%n");
        assertEquals(WRONG_MESSAGE_ERROR, expected, responses.get(1));
    }

    @Test
    public void testGetWalletEmptySummary() {
        List<Request> requests = new ArrayList<>();
        requests.add(new Request("guest", "register emptyWallet test"));
        requests.add(new Request("emptyWallet", "get-wallet-summary"));

        List<String> responses = processRequests(requests);
        assertNotNull(responses);

        String builder = "Wallet summary of emptyWallet:" + System.lineSeparator() +
                "Current balance: $0,0000" + System.lineSeparator() +
                "There are currently no active investments in your account." +
                System.lineSeparator();

        assertEquals(WRONG_MESSAGE_ERROR, builder, responses.get(1));
    }

    @Test
    public void testGetWalletSummaryWithNoInvestments() {
        List<Request> requests = new ArrayList<>();
        requests.add(new Request("guest", "register almostEmptyWallet test"));
        requests.add(new Request("almostEmptyWallet", "deposit-money 1234"));
        requests.add(new Request("almostEmptyWallet", "get-wallet-summary"));

        List<String> responses = processRequests(requests);
        assertNotNull(responses);

        String builder = "Wallet summary of almostEmptyWallet:" + System.lineSeparator() +
                "Current balance: $1234,0000" + System.lineSeparator() +
                "There are currently no active investments in your account." +
                System.lineSeparator();

        assertEquals(WRONG_MESSAGE_ERROR, builder, responses.get(2));
    }

    @Test
    public void testLogout() {
        List<Request> requests = new ArrayList<>();
        requests.add(new Request("guest", "register logout test"));
        requests.add(new Request("logout", "logout"));

        List<String> responses = processRequests(requests);
        assertNotNull(responses);

        String expected = String.format("You have successfully logged out.%n");

        assertEquals(WRONG_MESSAGE_ERROR, expected, responses.get(1));
    }

    @Test
    public void testQuitAsGuest() {
        Request request = new Request("guest", "quit");

        String response = processRequest(request);
        assertNotNull(response);

        String expected = String.format("You have disconnected from the server.%n");
        assertEquals(WRONG_MESSAGE_ERROR, expected, response);
    }

    @Test
    public void testQuitAsUser() {
        List<Request> requests = new ArrayList<>();
        requests.add(new Request("guest", "register quitter test"));
        requests.add(new Request("quitter", "quit"));

        List<String> responses = processRequests(requests);
        assertNotNull(responses);

        String expected = String.format("You have disconnected from the server.%n");
        assertEquals(WRONG_MESSAGE_ERROR, expected, responses.get(1));
    }

    private String processRequest(Request request) {
        CryptoWalletClient client = new CryptoWalletClient(SERVER_PORT);

        try {
            String responseMessage = client.processClientRequest(request);
            client.stop();

            return responseMessage;
        } catch (IOException e) {
            System.out.println("An error has occurred during client message processing: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    private List<String> processRequests(List<Request> requests) {
        CryptoWalletClient client = new CryptoWalletClient(SERVER_PORT);

        try {
            List<String> responses = new ArrayList<>();

            for (Request request : requests) {
                String responseMessage = client.processClientRequest(request);
                responses.add(responseMessage);
            }

            client.stop();
            return responses;
        } catch (IOException e) {
            System.out.println("An error has occurred during client message processing: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    @AfterClass
    public static void stopServer() {
        cryptoServer.stop();
        serverThread.interrupt();
    }
}
