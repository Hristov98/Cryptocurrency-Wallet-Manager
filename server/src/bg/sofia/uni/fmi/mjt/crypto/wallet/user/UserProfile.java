package bg.sofia.uni.fmi.mjt.crypto.wallet.user;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class UserProfile {
    private static final String INVALID_OFFERING_CODE_ERROR =
            String.format("The offering code must not be empty.%n");
    private static final String INVALID_INVESTMENT_ERROR =
            String.format("The money to invest must not be a negative number.%n");
    private static final String INVALID_PRICE_ERROR =
            String.format("The price of the cryptocurrency must not be a negative number.%n");
    private static final String INSUFFICIENT_FUNDS_ERROR =
            String.format("You are attempting to invest more money than you currently own. Please deposit first.%n");
    private static final String CURRENCY_NOT_FOUND_ERROR =
            String.format("You have not invested in the currency you are trying to sell. Please purchase first.%n");

    private final String username;
    private final String password;
    private final Map<String, CryptoCurrency> portfolio;
    private double balance;

    public UserProfile(String username, String password) {
        this.username = username;
        this.password = password;
        this.balance = 0;
        portfolio = new HashMap<>();
    }

    public String getPassword() {
        return password;
    }

    public double getBalance() {
        return balance;
    }

    public void depositMoney(double amountToDeposit) {
        balance += amountToDeposit;
    }

    public String buyCryptocurrency(String offeringCode, double moneyToInvest, double currencyPrice) {
        validateOfferingCodeIsNotEmpty(offeringCode);
        validateInvestmentIsPositive(moneyToInvest);
        validatePriceIsPositive(currencyPrice);
        validateUserHasSufficientFunds(moneyToInvest);

        balance -= moneyToInvest;
        double currencyPurchased = moneyToInvest / currencyPrice;

        if (!portfolio.containsKey(offeringCode)) {
            portfolio.put(offeringCode, new CryptoCurrency(offeringCode));
        }
        portfolio.get(offeringCode).addToCurrency(currencyPurchased, moneyToInvest);

        return String.format("Operation successful. You have purchased %.4f %s for $%f.%n",
                currencyPurchased, offeringCode, moneyToInvest);
    }

    private void validateOfferingCodeIsNotEmpty(String offeringCode) {
        if (offeringCode == null || offeringCode.length() == 0) {
            throw new IllegalArgumentException(INVALID_OFFERING_CODE_ERROR);
        }
    }

    private void validateInvestmentIsPositive(double moneyToInvest) {
        if (moneyToInvest <= 0) {
            throw new IllegalArgumentException(INVALID_INVESTMENT_ERROR);
        }
    }

    private void validateUserHasSufficientFunds(double moneyToInvest) {
        if (moneyToInvest > balance) {
            throw new IllegalArgumentException(INSUFFICIENT_FUNDS_ERROR);
        }
    }

    private void validatePriceIsPositive(double currencyPrice) {
        if (currencyPrice <= 0) {
            throw new IllegalArgumentException(INVALID_PRICE_ERROR);
        }
    }

    public String sellCryptocurrency(String offeringCode, double currencyPrice) {
        validateOfferingCodeIsNotEmpty(offeringCode);
        validateUserHasInvestedInCurrency(offeringCode);
        validatePriceIsPositive(currencyPrice);

        CryptoCurrency soldCurrency = portfolio.remove(offeringCode);
        double moneyReceived = soldCurrency.getCryptoAmount() * currencyPrice;
        balance += moneyReceived;

        return String.format("Operation successful. You have sold %.4f %s for $%f.%n",
                soldCurrency.getCryptoAmount(), offeringCode, moneyReceived);
    }

    private void validateUserHasInvestedInCurrency(String offeringCode) {
        if (!portfolio.containsKey(offeringCode)) {
            throw new IllegalArgumentException(CURRENCY_NOT_FOUND_ERROR);
        }
    }

    public String getWalletSummary() {
        StringBuilder builder = new StringBuilder();

        builder.append(String.format("Wallet summary of %s:%n", username));
        builder.append(String.format("Current balance: $%.4f%n", balance));
        if (portfolio.size() == 0) {
            builder.append(String.format("There are currently no active investments in your account.%n"));
        } else {
            int index = 1;
            for (CryptoCurrency currency : portfolio.values()) {
                builder.append(formatCurrencyInvestment(index, currency));
                builder.append(System.lineSeparator());
                index++;
            }
        }

        return builder.toString();
    }

    private String formatCurrencyInvestment(int index, CryptoCurrency currency) {
        return String.format("%2d) Offering code: %4s, Amount purchased: %.4f, Money invested: $%.4f",
                index, currency.getCode(), currency.getCryptoAmount(), currency.getTotalPrice());
    }

    public Set<String> getUserCryptoCurrencies() {
        return portfolio.keySet();
    }

    public String getWalletOverallSummary(Map<String, Double> currencyPrices) {
        StringBuilder builder = new StringBuilder();

        builder.append(String.format("Complete wallet summary of %s:%n", username));
        builder.append(String.format("Current balance: $%.4f%n", balance));
        if (portfolio.size() == 0) {
            builder.append(String.format("There are currently no active investments in your account.%n"));
        } else {
            int index = 1;
            for (CryptoCurrency currency : portfolio.values()) {
                double price = currencyPrices.get(currency.getCode());
                builder.append(formatCurrencyInvestment(index, currency));
                builder.append(formatCurrencyReturn(currency, price));
                index++;
            }
        }

        return builder.toString();
    }

    private String formatCurrencyReturn(CryptoCurrency currency, double price) {
        double boughtFor = currency.getTotalPrice();
        double soldFor = currency.getCryptoAmount() * price;

        double investmentResultUSD = soldFor - boughtFor;
        double investmentResultPercent = soldFor / boughtFor;

        if (investmentResultUSD < 0) {
            double prettyPercentage = 100 - investmentResultPercent * 100;
            return String.format(", Can sell for: $%.4f, Loss: %.6f%%%n",
                    soldFor, prettyPercentage);
        }

        double prettyPercentage = investmentResultPercent * 100 - 100;
        return String.format(", Can sell for: $%.4f, Gain: %.6f%%%n",
                soldFor, prettyPercentage);
    }
}
