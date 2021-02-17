package bg.sofia.uni.fmi.mjt.crypto.wallet.coinAPI;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class CachedCurrency {
    private double currentPrice;
    private LocalDateTime lastUpdate;

    public CachedCurrency(double currentPrice) {
        this.currentPrice = currentPrice;
        lastUpdate = LocalDateTime.now();
    }

    public double getCurrentPrice() {
        return currentPrice;
    }

    public boolean isOutOfDate() {
        return LocalDateTime.now().isAfter(lastUpdate.plus(30, ChronoUnit.MINUTES));
    }

    public void update(double updatedPrice) {
        currentPrice = updatedPrice;
        lastUpdate = LocalDateTime.now();
    }
}
