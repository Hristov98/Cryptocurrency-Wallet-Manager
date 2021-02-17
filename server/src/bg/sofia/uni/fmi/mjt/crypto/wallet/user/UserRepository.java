package bg.sofia.uni.fmi.mjt.crypto.wallet.user;

import bg.sofia.uni.fmi.mjt.crypto.wallet.communication.Response;
import bg.sofia.uni.fmi.mjt.crypto.wallet.exception.PasswordEncryptionException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class UserRepository {
    private static final String EMPTY_USERNAME_ERROR = "The passed username must not be empty";
    private static final String EMPTY_PASSWORD_ERROR = "The passed password must not be empty";
    private static final String TAKEN_USERNAME_ERROR =
            String.format("This username is already taken, please enter a valid one.%n");
    private static final String ILLEGAL_USERNAME_ERROR =
            String.format("This username contains illegal characters, please enter a valid one.%n");
    private static final String PASSWORD_ENCRYPTION_ERROR =
            "An error occurred while encrypting the password during user registration";
    private static final String INVALID_LOGIN_ERROR =
            String.format("The username or password you have entered is incorrect.%n");
    private static final String LOGOUT_ERROR =
            String.format("This account has already logged out completely.%n");
    private static final String SUCCESSFUL_LOGOUT_MESSAGE =
            String.format("You have successfully logged out.%n");

    private final Gson gson;
    private final Map<String, Integer> connectionsPerUser;
    private Map<String, UserProfile> users;

    public UserRepository() {
        gson = new GsonBuilder().setPrettyPrinting().create();
        connectionsPerUser = new HashMap<>();
        users = new HashMap<>();
    }

    public Response registerIfValid(String username, String password) {
        validateUsernameIsNotEmpty(username);
        validatePasswordIsNotEmpty(password);

        if (isRegistered(username)) {
            return new Response(false, "guest", TAKEN_USERNAME_ERROR);
        }
        if (!usernameIsValid(username)) {
            return new Response(false, "guest", ILLEGAL_USERNAME_ERROR);
        }

        registerUser(username, password);
        logIn(username);
        String successMessage = String.format("User %s has successfully been registered.%n", username);
        return new Response(true, username, successMessage);
    }

    private void validateUsernameIsNotEmpty(String username) {
        if (username == null || username.length() == 0) {
            throw new IllegalArgumentException(EMPTY_USERNAME_ERROR);
        }
    }

    private void validatePasswordIsNotEmpty(String password) {
        if (password == null || password.length() == 0) {
            throw new IllegalArgumentException(EMPTY_PASSWORD_ERROR);
        }
    }

    public boolean isRegistered(String username) {
        validateUsernameIsNotEmpty(username);

        return users.containsKey(username);
    }

    private boolean usernameIsValid(String username) {
        final String regex = "^[a-zA-Z0-9\\-_.]+$";

        return username.matches(regex);
    }

    private void registerUser(String username, String password) {
        String passwordHash = encryptPassword(password);
        UserProfile newUser = new UserProfile(username, passwordHash);

        users.put(username, newUser);
    }

    private String encryptPassword(String password) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] passwordHashBytes = messageDigest.digest(password.getBytes(StandardCharsets.UTF_8));
            return convertByteArrayToString(passwordHashBytes);
        } catch (NoSuchAlgorithmException exception) {
            throw new PasswordEncryptionException(PASSWORD_ENCRYPTION_ERROR, exception);
        }
    }

    private String convertByteArrayToString(byte[] passwordHashBytes) {
        BigInteger number = new BigInteger(1, passwordHashBytes);
        StringBuilder hexString = new StringBuilder(number.toString(16));
        while (hexString.length() < 32) {
            hexString.insert(0, '0');
        }

        return hexString.toString();
    }

    public Response logInIfValid(String username, String password) {
        validateUsernameIsNotEmpty(username);
        validatePasswordIsNotEmpty(password);

        if (!userCredentialsAreCorrect(username, password)) {
            return new Response(false, "guest", INVALID_LOGIN_ERROR);
        }

        logIn(username);
        String successMessage = String.format("You have successfully logged in. Welcome back, %s.%n", username);
        return new Response(true, username, successMessage);
    }

    private boolean userCredentialsAreCorrect(String username, String password) {
        if (!isRegistered(username)) {
            return false;
        }

        String passwordHash = encryptPassword(password);
        return passwordHash.equals(users.get(username).getPassword());
    }

    private void logIn(String username) {
        if (connectionsPerUser.containsKey(username)) {
            connectionsPerUser.put(username, connectionsPerUser.get(username) + 1);
        } else {
            connectionsPerUser.put(username, 1);
        }
    }

    public String logOut(String username) {
        validateUsernameIsNotEmpty(username);

        if (!connectionsPerUser.containsKey(username)) {
            return LOGOUT_ERROR;
        }

        int connectionsAfterLogout = connectionsPerUser.get(username) - 1;
        if (connectionsAfterLogout > 0) {
            connectionsPerUser.put(username, connectionsAfterLogout);
        } else {
            connectionsPerUser.remove(username);
        }

        return SUCCESSFUL_LOGOUT_MESSAGE;
    }

    public boolean isLoggedIn(String username) {
        validateUsernameIsNotEmpty(username);

        return connectionsPerUser.containsKey(username);
    }

    public UserProfile getUserByUsername(String username) {
        validateUsernameIsNotEmpty(username);

        return users.get(username);
    }

    public String serializeUsers() {
        UserDTO[] userWrapper = new UserDTO[users.size()];

        int index = 0;
        for (Map.Entry<String, UserProfile> user : users.entrySet()) {
            userWrapper[index++] = new UserDTO(user.getKey(), user.getValue());
        }

        return gson.toJson(userWrapper);
    }

    public void deserializeUsers(String usersJSON) {
        Gson gson = new Gson();

        UserDTO[] testUsers = gson.fromJson(usersJSON, UserDTO[].class);
        for (UserDTO user : testUsers) {
            users.put(user.getUsername(), user.getProfile());
        }
    }
}
