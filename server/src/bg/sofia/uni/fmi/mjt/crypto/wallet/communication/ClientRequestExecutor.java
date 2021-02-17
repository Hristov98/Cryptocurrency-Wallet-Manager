package bg.sofia.uni.fmi.mjt.crypto.wallet.communication;

import bg.sofia.uni.fmi.mjt.crypto.wallet.coinAPI.CoinAPI;
import bg.sofia.uni.fmi.mjt.crypto.wallet.exception.CoinAPIException;
import bg.sofia.uni.fmi.mjt.crypto.wallet.exception.UserDataLoadingException;
import bg.sofia.uni.fmi.mjt.crypto.wallet.user.UserProfile;
import bg.sofia.uni.fmi.mjt.crypto.wallet.user.UserRepository;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

public class ClientRequestExecutor {
    private static final String USER_FILE_LOCATION = String.format("%s%s%s",
            "resources", FileSystems.getDefault().getSeparator(), "users.json");
    private static final String USER_FILE_NOT_FOUND_ERROR =
            "User file not found. User loading has been aborted.";
    private static final String USER_LOADING_CONVERSION_ERROR =
            "The user objects could not be converted correctly. User loading has been aborted.";
    private static final String USER_LOADING_ERROR =
            "A problem occurred while reading user file. User loading has been aborted.";
    private static final String UNKNOWN_COMMAND_MESSAGE =
            String.format("You have entered an unknown command.%n");
    private static final String DEPOSIT_CONVERSION_ERROR =
            String.format("Deposit cancelled. The deposit amount must be a valid number.%n");
    private static final String DEPOSIT_NEGATIVE_NUMBER_ERROR =
            String.format("Deposit cancelled. The deposit amount must be a positive number.%n");
    private static final String PURCHASE_CONVERSION_ERROR =
            String.format("Purchase cancelled. The purchase amount must be a valid number.%n");
    private static final String PURCHASE_NEGATIVE_NUMBER_ERROR =
            String.format("Purchase cancelled. The purchase amount must be a positive number.%n");
    private static final String SUCCESSFUL_DISCONNECT_MESSAGE =
            String.format("You have disconnected from the server.%n");
    private static final String USER_SAVING_ERROR =
            String.format("An error occurred while saving users.%n");

    private final UserRepository userRepository;
    private final CoinAPI coinAPI;
    private final Gson gson;

    public ClientRequestExecutor() {
        userRepository = new UserRepository();
        coinAPI = new CoinAPI();
        gson = new Gson();

        loadUsers();
    }

    private void loadUsers() {
        System.out.println("Loading users...");
        Path userFilePath = Path.of(USER_FILE_LOCATION);

        try {
            loadUsersFromFile(userFilePath);
        } catch (UserDataLoadingException exception) {
            System.out.println(exception.getMessage());
        }
    }

    private void loadUsersFromFile(Path userFilePath) {
        try {
            validateUserFileExists(userFilePath);

            String usersJSON = Files.readString(userFilePath);
            userRepository.deserializeUsers(usersJSON);
        } catch (JsonSyntaxException exception) {
            throw new UserDataLoadingException(USER_LOADING_CONVERSION_ERROR, exception);
        } catch (IOException exception) {
            throw new UserDataLoadingException(USER_LOADING_ERROR, exception);
        }

        System.out.println("Users successfully loaded");
    }

    private void validateUserFileExists(Path userFilePath) {
        if (!Files.exists(userFilePath)) {
            throw new UserDataLoadingException(USER_FILE_NOT_FOUND_ERROR);
        }
    }

    public String processRequest(String requestJSON) {
        Request clientRequest = getRequestFromJSON(requestJSON);

        Response response;
        if (clientRequest.isFromGuest()) {
            response = processGuestRequest(clientRequest.getSender(), clientRequest.getCommand());
        } else {
            response = processUserRequest(clientRequest.getSender(), clientRequest.getCommand());
        }

        return gson.toJson(response);
    }

    private Request getRequestFromJSON(String requestJSON) {
        return gson.fromJson(requestJSON, Request.class);
    }

    private Response processGuestRequest(String username, String fullCommand) {
        String command = fullCommand.split("\\s+")[0];

        switch (command) {
            case "register" -> {
                return registerUser(fullCommand);
            }
            case "login" -> {
                return logInUser(fullCommand);
            }
            case "quit" -> {
                return disconnectUser(username);
            }
            default -> {
                return getNotificationOfWrongCommand();
            }
        }
    }

    private Response registerUser(String clientRequest) {
        String[] splitRequest = clientRequest.split("\\s+");
        if (splitRequest.length != 3) {
            return getNotificationOfWrongCommand();
        }

        String username = splitRequest[1];
        String password = splitRequest[2];
        return userRepository.registerIfValid(username, password);
    }

    private Response getNotificationOfWrongCommand() {
        return new Response(false, "unknown", UNKNOWN_COMMAND_MESSAGE);
    }

    private Response logInUser(String clientRequest) {
        String[] splitRequest = clientRequest.split("\\s+");
        if (splitRequest.length != 3) {
            return getNotificationOfWrongCommand();
        }

        String username = splitRequest[1];
        String password = splitRequest[2];
        return userRepository.logInIfValid(username, password);
    }

    private Response disconnectUser(String username) {
        if (userRepository.isLoggedIn(username)) {
            userRepository.logOut(username);
        }

        return new Response(true, "guest", SUCCESSFUL_DISCONNECT_MESSAGE);
    }

    private Response processUserRequest(String username, String fullCommand) {
        String command = fullCommand.split("\\s+")[0];

        switch (command) {
            case "deposit-money" -> {
                return depositMoney(username, fullCommand);
            }
            case "list-offerings" -> {
                return listOfferings();
            }
            case "buy" -> {
                return buyCryptocurrency(username, fullCommand);
            }
            case "sell" -> {
                return sellCryptocurrency(username, fullCommand);
            }
            case "get-wallet-summary" -> {
                return getWalletSummary(username);
            }
            case "get-wallet-overall-summary" -> {
                return getWalletOverallSummary(username);
            }
            case "logout" -> {
                return logOutUser(username);
            }
            case "quit" -> {
                return disconnectUser(username);
            }
            case "save-users" -> {
                return saveUsersToFile();
            }
            default -> {
                return getNotificationOfWrongCommand();
            }
        }
    }

    private Response depositMoney(String username, String clientRequest) {
        String[] splitRequest = clientRequest.split("\\s+");
        if (splitRequest.length != 2) {
            return getNotificationOfWrongCommand();
        }

        try {
            return attemptDeposit(username, splitRequest[1]);
        } catch (NumberFormatException exception) {
            return new Response(false, "user", DEPOSIT_CONVERSION_ERROR);
        }
    }

    private Response attemptDeposit(String username, String number) {
        double depositAmount = Double.parseDouble(number);

        String resultMessage;
        if (depositAmount <= 0) {
            return new Response(false, "user", DEPOSIT_NEGATIVE_NUMBER_ERROR);
        }

        UserProfile user = userRepository.getUserByUsername(username);
        user.depositMoney(depositAmount);
        resultMessage = String.format("Deposit successful. Current balance: $%.4f.%n", user.getBalance());
        return new Response(true, "user", resultMessage);
    }

    private Response listOfferings() {
        try {
            String offeringsList = coinAPI.getListOfCryptoCurrencies();
            return new Response(true, "user", offeringsList);
        } catch (CoinAPIException exception) {
            return new Response(false, "user", exception.getMessage());
        }
    }

    private Response buyCryptocurrency(String username, String clientRequest) {
        String[] splitRequest = clientRequest.split("\\s+");
        if (splitRequest.length != 3) {
            return getNotificationOfWrongCommand();
        }

        try {
            return attemptPurchase(username, splitRequest[1].toUpperCase(), splitRequest[2]);
        } catch (NumberFormatException exception) {
            return new Response(false, "user", PURCHASE_CONVERSION_ERROR);
        } catch (IllegalArgumentException | CoinAPIException exception) {
            return new Response(false, "user", exception.getMessage());
        }
    }

    private Response attemptPurchase(String username, String offeringCode, String number) {
        double moneyToInvest = Double.parseDouble(number);
        if (moneyToInvest <= 0) {
            return new Response(false, "user", PURCHASE_NEGATIVE_NUMBER_ERROR);
        }

        UserProfile user = userRepository.getUserByUsername(username);
        double currencyPrice = coinAPI.getCoinCurrentPrice(offeringCode);

        String resultMessage = user.buyCryptocurrency(offeringCode, moneyToInvest, currencyPrice);
        return new Response(true, "user", resultMessage);
    }

    private Response sellCryptocurrency(String username, String clientRequest) {
        String[] splitRequest = clientRequest.split("\\s+");
        if (splitRequest.length != 2) {
            return getNotificationOfWrongCommand();
        }

        try {
            return attemptSell(username, splitRequest[1].toUpperCase());
        } catch (NumberFormatException exception) {
            return new Response(false, "user", PURCHASE_CONVERSION_ERROR);
        } catch (IllegalArgumentException | CoinAPIException exception) {
            return new Response(false, "user", exception.getMessage());
        }
    }

    private Response attemptSell(String username, String offeringCode) {
        UserProfile user = userRepository.getUserByUsername(username);
        double currencyPrice = coinAPI.getCoinCurrentPrice(offeringCode);

        String resultMessage = user.sellCryptocurrency(offeringCode, currencyPrice);
        return new Response(true, "user", resultMessage);
    }

    public Response getWalletSummary(String username) {
        UserProfile userProfile = userRepository.getUserByUsername(username);
        String walletSummaryResult = userProfile.getWalletSummary();

        return new Response(true, "user", walletSummaryResult);
    }

    public Response getWalletOverallSummary(String username) {
        UserProfile userProfile = userRepository.getUserByUsername(username);
        Set<String> userOfferingCodes = userProfile.getUserCryptoCurrencies();
        Map<String, Double> currencyPrices = coinAPI.getSpecificCurrentPrices(userOfferingCodes);
        String fullWalletSummaryResult = userProfile.getWalletOverallSummary(currencyPrices);

        return new Response(true, "user", fullWalletSummaryResult);
    }

    private Response logOutUser(String username) {
        String resultMessage = userRepository.logOut(username);
        return new Response(true, "guest", resultMessage);
    }

    public Response saveUsersToFile() {
        System.out.println("Saving users...");
        Path userFilePath = Path.of(USER_FILE_LOCATION);

        try {
            if (!Files.exists(userFilePath)) {
                Files.createDirectories(Path.of("resources"));
                Files.createFile(userFilePath);
            }

            String usersJSON = userRepository.serializeUsers();
            Files.writeString(userFilePath, usersJSON);
        } catch (IOException exception) {
            return new Response(false, "user", USER_SAVING_ERROR);
        }

        return new Response(true, "user", String.format("Users successfully saved.%n"));
    }
}
