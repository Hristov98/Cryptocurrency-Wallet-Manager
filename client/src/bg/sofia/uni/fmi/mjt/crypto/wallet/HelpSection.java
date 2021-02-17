package bg.sofia.uni.fmi.mjt.crypto.wallet;

public class HelpSection {
    public static final String REGISTER_INFORMATION = """
            register [username] [password] - Registers a new account into the system. If the registration is 
            successful, the user will automatically be logged into the system.
                        
            This command has two arguments:
            1) username - The username of the new account. If a user with the same username is already 
            registered into the system, the request will be cancelled.
            2) password - The password of the new account.
            """;

    public static final String LOGIN_INFORMATION = """
            login [username] [password] - Logs a user into the system. The login will be successful if both 
            username and password match. The user may log into his account multiple times from different 
            connections.
                        
            This command has two arguments:
            1) username - The username of the user's account.
            2) password - The password of the user's account.
            """;

    public static final String DEPOSIT_MONEY_INFORMATION = """
            deposit-money [amount_in_USD] - Deposits the specified amount of money into the user's account.
                        
            This command has a single argument:
            1) amount_in_USD - The amount of USD that will be added to the account. If the argument is not a
            positive or a valid number, the request will be cancelled.
            """;

    public static final String LIST_OFFERINGS_INFORMATION = """
            list-offerings - Presents a list of cryptocurrencies to the user sorted by price in descending
            order. The price of the cryptocurrencies are determined by fetching them from CoinAPI.
                       
            Warning: CoinAPI's free API key limits requests to 100 per day, so any requests after that limit 
            is reached will be unsuccessful.
            """;

    public static final String BUY_INFORMATION = """
            buy [offering_code] [amount_to_pay] - Invests the money specified by the user into a cryptocurrency.
            The price of the cryptocurrency is determined by fetching it from CoinAPI.
                        
            This command has two arguments:
            1) offering_code - The unique ID of the cryptocurrency. If this argument isn't an exact match, the 
            request will be cancelled or the user will purchase the wrong cryptocurrency. To avoid this, please
            call "list-offerings" first to get a list of offering codes associated with each currency.
            2) amount_to_pay - The amount of USD that the user will invest. If the argument is not a positive 
            or a valid number, the request will be cancelled.
                        
            Warning: CoinAPI's free API key limits requests to 100 per day, so any requests after that limit 
            is reached will be unsuccessful.
            """;

    public static final String SELL_INFORMATION = """
            sell [offering_code] - Sells the entirety of a cryptocurrency and add the earnings to the user's
            balance. The price of the cryptocurrency is determined by fetching it from CoinAPI.
                        
            This command has a single argument:
            1) offering_code - The unique ID of the cryptocurrency. The user may only sell cryptocurrencies he
            has invested in. Otherwise, this request will be cancelled. 
                        
            Warning: CoinAPI's free API key limits requests to 100 per day, so any requests after that limit 
            is reached will be unsuccessful.
            """;

    public static final String WALLET_SUMMARY_INFORMATION = """
            get-wallet-summary - Displays a list of the user's portfolio. The information contained within 
            includes:
            1) The user's current balance
            2) The amount of cryptocurrency held
            3) The money invested in each cryptocurrency
            """;

    public static final String FULL_WALLET_SUMMARY_INFORMATION = """
            get-wallet-overall-summary - Displays a detailed list of the user's portfolio. The information
            contained within includes:
            1) The user's current balance
            2) The amount of cryptocurrency held
            3) The money invested in each cryptocurrency
            4) The amount of money received if the user chooses to sell each cryptocurrency
            5) The percentage of profit/loss if the user chooses to sell each cryptocurrency
            To calculate 4) and 5) the command will look up the current price of cryptocurrencies
            through CoinAPI.
                        
            Warning: CoinAPI's free API key limits requests to 100 per day, so any requests after that limit 
            is reached will be unsuccessful. Since this command updates each currency individually, the user 
            is recommended to minimize requests called by first making sure all currencies are up to date 
            with command "list-offerings".
            """;

    public static final String LOGOUT_INFORMATION = """
            logout - Logs the user out of the system. The user will retain his connection to the server as
            a guest.
            """;

    public static final String HELP_INFORMATION = """
            help [command_name] - Gives detailed information about the specified command and how to use it.
                        
            This command has a single argument:
            1) command_name - The name of the command to look up.
            """;

    public static final String QUIT_INFORMATION = """
            quit - Terminates the user's connection to the server. If the user is logged in, they will first 
            be logged out before disconnecting.
            """;

    public static final String WRONG_COMMAND_ERROR =
            String.format("You are trying to look up an unknown command.%n");
    public static final String EMPTY_STRING_ERROR =
            String.format("Help command has been called with an empty string. Please try again.%n");
    public static final String INCORRECT_FORMAT_ERROR =
            String.format("Help command has been called in an incorrect format. Please try again.%n");

    public static String getInformation(String helpRequest) {
        if (helpRequest == null || helpRequest.length() == 0) {
            return EMPTY_STRING_ERROR;
        }

        String[] commandArray = helpRequest.split("\\s+");
        if (commandArray.length != 2) {
            return INCORRECT_FORMAT_ERROR;
        }

        String commandToSpecify = commandArray[1];
        return processHelpRequest(commandToSpecify);
    }

    public static String processHelpRequest(String command) {
        switch (command) {
            case "register" -> {
                return REGISTER_INFORMATION;
            }
            case "login" -> {
                return LOGIN_INFORMATION;
            }
            case "deposit-money" -> {
                return DEPOSIT_MONEY_INFORMATION;
            }
            case "list-offerings" -> {
                return LIST_OFFERINGS_INFORMATION;
            }
            case "buy" -> {
                return BUY_INFORMATION;
            }
            case "sell" -> {
                return SELL_INFORMATION;
            }
            case "get-wallet-summary" -> {
                return WALLET_SUMMARY_INFORMATION;
            }
            case "get-wallet-overall-summary" -> {
                return FULL_WALLET_SUMMARY_INFORMATION;
            }
            case "logout" -> {
                return LOGOUT_INFORMATION;
            }
            case "help" -> {
                return HELP_INFORMATION;
            }
            case "quit" -> {
                return QUIT_INFORMATION;
            }
            default -> {
                return WRONG_COMMAND_ERROR;
            }
        }
    }
}
