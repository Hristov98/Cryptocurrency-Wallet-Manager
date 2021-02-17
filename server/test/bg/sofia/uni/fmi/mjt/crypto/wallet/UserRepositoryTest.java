package bg.sofia.uni.fmi.mjt.crypto.wallet;

import bg.sofia.uni.fmi.mjt.crypto.wallet.user.UserRepository;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class UserRepositoryTest {
    private static final String WRONG_MESSAGE_ERROR = "The result is incorrect or formatted incorrectly";
    private UserRepository userRepository;

    @Before
    public void setUpProfile() {
        userRepository = new UserRepository();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRegistrationWithEmptyUsername() {
        userRepository.registerIfValid(null, "test");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRegistrationWithEmptyPassword() {
        userRepository.registerIfValid("test", null);
    }

    @Test
    public void testLogoutWithInvalidUser() {
        String result = userRepository.logOut("test");

        String expected = String.format("This account has already logged out completely.%n");
        assertEquals(WRONG_MESSAGE_ERROR, expected, result);
    }

    @Test
    public void testLogoutWithUserLoggedInOnce() {
        userRepository.registerIfValid("test", "test");
        String result = userRepository.logOut("test");

        String expected = String.format("You have successfully logged out.%n");
        assertEquals(WRONG_MESSAGE_ERROR, expected, result);
    }

    @Test
    public void testLogoutWithUserLoggedMultipleTimes() {
        userRepository.registerIfValid("test", "test");
        userRepository.logInIfValid("test", "test");
        userRepository.logInIfValid("test", "test");
        userRepository.logInIfValid("test", "test");
        String result = userRepository.logOut("test");

        String expected = String.format("You have successfully logged out.%n");
        assertEquals(WRONG_MESSAGE_ERROR, expected, result);
    }
}
