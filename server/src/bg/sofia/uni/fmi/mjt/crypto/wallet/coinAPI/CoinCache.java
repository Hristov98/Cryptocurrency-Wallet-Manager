package bg.sofia.uni.fmi.mjt.crypto.wallet.coinAPI;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CoinCache {
    private static final int OUT_OF_DATE_CURRENCIES_LIMIT = 5;
    private final Map<String, CachedCurrency> cache;

    public CoinCache() {
        cache = new LinkedHashMap<>();
    }

    public boolean isEmpty() {
        return cache.size() == 0;
    }

    public void addListOfCurrencies(List<CurrencyDTO> currencies) {
        for (CurrencyDTO currency : currencies) {
            addCurrency(currency.getAssetId(), currency.getPriceUSD());
        }
    }

    public void addCurrency(String assetID, double currentPrice) {
        cache.put(assetID, new CachedCurrency(currentPrice));
    }

    public boolean isOutOfDate() {
        int outOfDateCounter = 0;
        for (CachedCurrency currency : cache.values()) {
            if (currency.isOutOfDate()) {
                outOfDateCounter++;
            }
        }

        return outOfDateCounter >= OUT_OF_DATE_CURRENCIES_LIMIT;
    }

    public void updateListOfCurrencies(List<CurrencyDTO> currencies) {
        for (CurrencyDTO currency : currencies) {
            updateCurrency(currency.getAssetId(), currency.getPriceUSD());
        }
    }

    public void updateCurrency(String assetID, double currentPrice) {
        cache.get(assetID).update(currentPrice);
    }

    public String formatListOfOfferings() {
        StringBuilder builder = new StringBuilder();
        builder.append("List of offerings: ").append(System.lineSeparator());

        int offeringIndex = 1;
        for (Map.Entry<String, CachedCurrency> currency : cache.entrySet()) {
            builder.append(String.format("%2d) Offering code: %4s, Current price: $%f%n",
                    offeringIndex, currency.getKey(), currency.getValue().getCurrentPrice()));

            offeringIndex++;
        }

        return builder.toString();
    }

    public boolean containsCurrency(String offeringCode) {
        return cache.containsKey(offeringCode);
    }

    public CachedCurrency getCurrency(String offeringCode) {
        return cache.get(offeringCode);
    }
}
