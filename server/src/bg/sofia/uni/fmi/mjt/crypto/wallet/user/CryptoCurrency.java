package bg.sofia.uni.fmi.mjt.crypto.wallet.user;

public class CryptoCurrency {
    private final String code;
    private double cryptoAmount;
    private double totalPrice;

    public CryptoCurrency(String code) {
        this.code = code;
        cryptoAmount = 0;
        totalPrice = 0;
    }

    public String getCode() {
        return code;
    }

    public double getCryptoAmount() {
        return cryptoAmount;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void addToCurrency(double amount, double price) {
        cryptoAmount += amount;
        totalPrice += price;
    }
}
