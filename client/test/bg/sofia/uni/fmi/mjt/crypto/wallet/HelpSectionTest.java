package bg.sofia.uni.fmi.mjt.crypto.wallet;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class HelpSectionTest {
    private static final String WRONG_INFORMATION_ERROR = "The wrong command has been returned";


    @Test
    public void testGetInformationNullInput() {
        String expectedErrorMessage = HelpSection.EMPTY_STRING_ERROR;
        String result = HelpSection.getInformation(null);

        assertEquals(WRONG_INFORMATION_ERROR, expectedErrorMessage, result);
    }

    @Test
    public void testGetInformationEmptyInput() {
        String expectedErrorMessage = HelpSection.EMPTY_STRING_ERROR;
        String result = HelpSection.getInformation("");

        assertEquals(WRONG_INFORMATION_ERROR, expectedErrorMessage, result);
    }

    @Test
    public void testGetInformationWithNotEnoughArguments() {
        String expectedErrorMessage = HelpSection.INCORRECT_FORMAT_ERROR;
        String result = HelpSection.getInformation("help");

        assertEquals(WRONG_INFORMATION_ERROR, expectedErrorMessage, result);
    }

    @Test
    public void testGetInformationWithTooManyArguments() {
        String expectedErrorMessage = HelpSection.INCORRECT_FORMAT_ERROR;
        String result = HelpSection.getInformation("help help help");

        assertEquals(WRONG_INFORMATION_ERROR, expectedErrorMessage, result);
    }

    @Test
    public void testGetInformationForUnknownCommand() {
        String expectedErrorMessage = HelpSection.WRONG_COMMAND_ERROR;
        String result = HelpSection.getInformation("help unknown");

        assertEquals(WRONG_INFORMATION_ERROR, expectedErrorMessage, result);
    }

    @Test
    public void testGetInformationAboutRegister() {
        String expectedErrorMessage = HelpSection.REGISTER_INFORMATION;
        String result = HelpSection.getInformation("help register");

        assertEquals(WRONG_INFORMATION_ERROR, expectedErrorMessage, result);
    }

    @Test
    public void testGetInformationAboutLogin() {
        String expectedErrorMessage = HelpSection.LOGIN_INFORMATION;
        String result = HelpSection.getInformation("help login");

        assertEquals(WRONG_INFORMATION_ERROR, expectedErrorMessage, result);
    }

    @Test
    public void testGetInformationAboutDepositMoney() {
        String expectedErrorMessage = HelpSection.DEPOSIT_MONEY_INFORMATION;
        String result = HelpSection.getInformation("help deposit-money");

        assertEquals(WRONG_INFORMATION_ERROR, expectedErrorMessage, result);
    }

    @Test
    public void testGetInformationAboutListOferrings() {
        String expectedErrorMessage = HelpSection.LIST_OFFERINGS_INFORMATION;
        String result = HelpSection.getInformation("help list-offerings");

        assertEquals(WRONG_INFORMATION_ERROR, expectedErrorMessage, result);
    }

    @Test
    public void testGetInformationAboutBuy() {
        String expectedErrorMessage = HelpSection.BUY_INFORMATION;
        String result = HelpSection.getInformation("help buy");

        assertEquals(WRONG_INFORMATION_ERROR, expectedErrorMessage, result);
    }

    @Test
    public void testGetInformationAboutSell() {
        String expectedErrorMessage = HelpSection.SELL_INFORMATION;
        String result = HelpSection.getInformation("help sell");

        assertEquals(WRONG_INFORMATION_ERROR, expectedErrorMessage, result);
    }

    @Test
    public void testGetInformationAboutGetWalletSummary() {
        String expectedErrorMessage = HelpSection.WALLET_SUMMARY_INFORMATION;
        String result = HelpSection.getInformation("help get-wallet-summary");

        assertEquals(WRONG_INFORMATION_ERROR, expectedErrorMessage, result);
    }

    @Test
    public void testGetInformationAboutGetWalletOverallSummary() {
        String expectedErrorMessage = HelpSection.FULL_WALLET_SUMMARY_INFORMATION;
        String result = HelpSection.getInformation("help get-wallet-overall-summary");

        assertEquals(WRONG_INFORMATION_ERROR, expectedErrorMessage, result);
    }

    @Test
    public void testGetInformationAboutLogout() {
        String expectedErrorMessage = HelpSection.LOGOUT_INFORMATION;
        String result = HelpSection.getInformation("help logout");

        assertEquals(WRONG_INFORMATION_ERROR, expectedErrorMessage, result);
    }

    @Test
    public void testGetInformationAboutHelp() {
        String expectedErrorMessage = HelpSection.HELP_INFORMATION;
        String result = HelpSection.getInformation("help help");

        assertEquals(WRONG_INFORMATION_ERROR, expectedErrorMessage, result);
    }

    @Test
    public void testGetInformationAboutQuit() {
        String expectedErrorMessage = HelpSection.QUIT_INFORMATION;
        String result = HelpSection.getInformation("help quit");

        assertEquals(WRONG_INFORMATION_ERROR, expectedErrorMessage, result);
    }
}
