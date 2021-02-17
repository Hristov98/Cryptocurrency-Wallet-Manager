package bg.sofia.uni.fmi.mjt.crypto.wallet;

import bg.sofia.uni.fmi.mjt.crypto.wallet.user.UserProfile;
import org.junit.Before;
import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class UserProfileTest {
    private static final String WRONG_MESSAGE_ERROR = "The result is incorrect or formatted incorrectly";
    private UserProfile testProfile;

    @Before
    public void setUpProfile() {
        testProfile = new UserProfile("admin",
                "8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuyCryptocurrencyWithNullOfferingCode() {
        testProfile.buyCryptocurrency(null, 1, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuyCryptocurrencyWithNonPositiveInvestment() {
        testProfile.buyCryptocurrency("test", 0, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuyCryptocurrencyWithNonPositivePrice() {
        testProfile.buyCryptocurrency("test", 1, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuyCryptocurrencyWithInsufficientFunds() {
        testProfile.buyCryptocurrency("test", 1500, 1.5);
    }

    @Test
    public void testBuyCryptocurrencyWithValidFunds() {
        testProfile.depositMoney(1500);
        String result = testProfile.buyCryptocurrency("test", 1500, 1.5);
        String expected = String.format("Operation successful. " +
                "You have purchased 1000,0000 test for $1500,000000.%n");

        assertEquals(WRONG_MESSAGE_ERROR, expected, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSellCryptocurrencyWithNullOfferingCode() {
        testProfile.sellCryptocurrency(null, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSellCryptocurrencyThatUserHasNotInvestedIn() {
        testProfile.sellCryptocurrency("test", 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSellCryptocurrencyWithNonPositivePrice() {
        testProfile.buyCryptocurrency("test", 1500, 1.5);
        testProfile.sellCryptocurrency("test", -1);
    }

    @Test
    public void testSellValidCryptocurrency() {
        testProfile.depositMoney(1500);
        testProfile.buyCryptocurrency("test", 1500, 1.5);

        String result = testProfile.sellCryptocurrency("test", 2);

        String expected = String.format("Operation successful. " +
                "You have sold 1000,0000 test for $2000,000000.%n");

        assertEquals(WRONG_MESSAGE_ERROR, expected, result);
    }

    @Test
    public void testGetWalletSummaryWithInvestments() {
        testProfile.depositMoney(30000);
        testProfile.buyCryptocurrency("ETH", 10000, 2400.245);
        testProfile.buyCryptocurrency("BTC", 10000, 47012.343);
        testProfile.buyCryptocurrency("test", 5000, 32.111);
        testProfile.buyCryptocurrency("GME", 5000, 98.232);

        String result = testProfile.getWalletSummary();
        String expected = "Wallet summary of admin:" + System.lineSeparator() +
                "Current balance: $0,0000" + System.lineSeparator() +
                " 1) Offering code:  BTC, Amount purchased: 0,2127, Money invested: $10000,0000" +
                System.lineSeparator() +
                " 2) Offering code: test, Amount purchased: 155,7099, Money invested: $5000,0000" +
                System.lineSeparator() +
                " 3) Offering code:  ETH, Amount purchased: 4,1662, Money invested: $10000,0000" +
                System.lineSeparator() +
                " 4) Offering code:  GME, Amount purchased: 50,8999, Money invested: $5000,0000" +
                System.lineSeparator();

        assertEquals(WRONG_MESSAGE_ERROR, expected, result);
    }

    @Test
    public void testGetOverallWalletSummaryWithNoInvestments() {
        Map<String, Double> newPrices = new LinkedHashMap<>();
        String result = testProfile.getWalletOverallSummary(newPrices);

        String expected = "Complete wallet summary of admin:" + System.lineSeparator() +
                "Current balance: $0,0000" + System.lineSeparator() +
                "There are currently no active investments in your account." + System.lineSeparator();
        assertEquals(WRONG_MESSAGE_ERROR, expected, result);
    }

    @Test
    public void testGetOverallWalletSummaryWithInvestments() {
        testProfile.depositMoney(30000);
        testProfile.buyCryptocurrency("ETH", 10000, 2400.245);
        testProfile.buyCryptocurrency("BTC", 10000, 47012.343);
        testProfile.buyCryptocurrency("test", 5000, 32.111);
        testProfile.buyCryptocurrency("GME", 5000, 98.232);

        Map<String, Double> newPrices = new LinkedHashMap<>();
        newPrices.put("ETH", 2500.0043);
        newPrices.put("BTC", 50012.852);
        newPrices.put("test", 32.111);
        newPrices.put("GME", 49.9);


        String result = testProfile.getWalletOverallSummary(newPrices);

        String expected = "Complete wallet summary of admin:" + System.lineSeparator() +
                "Current balance: $0,0000" + System.lineSeparator() +
                " 1) Offering code:  BTC, Amount purchased: 0,2127, Money invested: $10000,0000, " +
                "Can sell for: $10638,2386, Gain: 6,382386%" + System.lineSeparator() +
                " 2) Offering code: test, Amount purchased: 155,7099, Money invested: $5000,0000, " +
                "Can sell for: $5000,0000, Gain: 0,000000%" + System.lineSeparator() +
                " 3) Offering code:  ETH, Amount purchased: 4,1662, Money invested: $10000,0000, " +
                "Can sell for: $10415,6213, Gain: 4,156213%" + System.lineSeparator() +
                " 4) Offering code:  GME, Amount purchased: 50,8999, Money invested: $5000,0000, " +
                "Can sell for: $2539,9055, Loss: 49,201889%" + System.lineSeparator();
        assertEquals(WRONG_MESSAGE_ERROR, expected, result);
    }
}
