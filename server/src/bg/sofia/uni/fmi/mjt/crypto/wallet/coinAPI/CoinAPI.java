package bg.sofia.uni.fmi.mjt.crypto.wallet.coinAPI;

import bg.sofia.uni.fmi.mjt.crypto.wallet.exception.CoinAPIException;
import com.google.gson.Gson;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

public class CoinAPI {
    private static final String API_KEY = "REPLACE THIS TEXT WITH API KEY";
    private static final String ASSET_REQUEST_URI = "https://rest.coinapi.io/v1/assets";
    private static final String HTTP_REQUEST_ERROR = "Error occurred while executing HTTP request";
    private static final String REQUEST_FAILED_MESSAGE =
            String.format("Could not get list of crypto currencies at this time. Please try again later.%n");
    private static final String CURRENCY_NOT_FOUND_ERROR =
            String.format("The currency you have entered could not be found. Please enter again.%n");

    private final HttpClient coinAPIClient;
    private final CoinCache cache;
    private final Gson gson;

    public CoinAPI() {
        coinAPIClient = HttpClient.newBuilder().build();
        cache = new CoinCache();
        gson = new Gson();
    }

    public String getListOfCryptoCurrencies() {
        validateCacheIsUpToDate();

        return cache.formatListOfOfferings();
    }

    private void validateCacheIsUpToDate() {
        if (cache.isEmpty()) {
            initialiseCache();
        } else if (cache.isOutOfDate()) {
            refreshCache();
        }
    }

    private void initialiseCache() {
        List<CurrencyDTO> validCurrencies = getCurrenciesFromHTTPRequest();

        if (validCurrencies == null) {
            throw new CoinAPIException(REQUEST_FAILED_MESSAGE);
        }

        cache.addListOfCurrencies(validCurrencies);
    }

    private void refreshCache() {
        List<CurrencyDTO> validCurrencies = getCurrenciesFromHTTPRequest();

        if (validCurrencies == null) {
            throw new CoinAPIException(REQUEST_FAILED_MESSAGE);
        }

        cache.updateListOfCurrencies(validCurrencies);
    }

    private List<CurrencyDTO> getCurrenciesFromHTTPRequest() {
        HttpRequest request = createRequestFromURI(ASSET_REQUEST_URI);
        String coinsJSON = getResponseFromAPI(request);

        if (coinsJSON.startsWith("Error")) {
            return null;
        }

        CurrencyDTO[] coinsFromResponse = gson.fromJson(coinsJSON, CurrencyDTO[].class);
        return getCryptoCoins(coinsFromResponse);
    }

    private HttpRequest createRequestFromURI(String requestURI) {
        return HttpRequest.newBuilder()
                .uri(URI.create(requestURI))
                .GET()
                .header("X-CoinAPI-Key", API_KEY)
                .build();
    }

    private String getResponseFromAPI(HttpRequest request) {
        try {
            HttpResponse<String> response = coinAPIClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return response.body();
            } else {
                return "Error: Status code " + response.statusCode();
            }
        } catch (InterruptedException | IOException exception) {
            throw new CoinAPIException(HTTP_REQUEST_ERROR, exception);
        }
    }

    private List<CurrencyDTO> getCryptoCoins(CurrencyDTO[] coins) {
        return Arrays.stream(coins)
                .filter(CurrencyDTO::isCrypto)
                .filter(CurrencyDTO::hasValidPrice)
                .limit(50)
                .sorted(Collections.reverseOrder())
                .collect(Collectors.toList());
    }

    public double getCoinCurrentPrice(String offeringCode) {
        validateCurrencyIsUpToDate(offeringCode);

        CachedCurrency currency = cache.getCurrency(offeringCode);
        return currency.getCurrentPrice();
    }

    private void validateCurrencyIsUpToDate(String offeringCode) {
        if (!cache.containsCurrency(offeringCode)) {
            CurrencyDTO currency = getCurrencyFromHTTPRequest(offeringCode);
            cache.addCurrency(currency.getAssetId(), currency.getPriceUSD());
        } else if (cache.getCurrency(offeringCode).isOutOfDate()) {
            CurrencyDTO currency = getCurrencyFromHTTPRequest(offeringCode);
            cache.updateCurrency(currency.getAssetId(), currency.getPriceUSD());
        }
    }

    private CurrencyDTO getCurrencyFromHTTPRequest(String offeringCode) {
        final String completeURI = String.format("%s/%s", ASSET_REQUEST_URI, offeringCode);
        HttpRequest request = createRequestFromURI(completeURI);
        String coinsJSON = getResponseFromAPI(request);

        if (coinsJSON.startsWith("Error")) {
            throw new CoinAPIException(REQUEST_FAILED_MESSAGE);
        }

        CurrencyDTO[] resultArray = gson.fromJson(coinsJSON, CurrencyDTO[].class);
        if (resultArray.length == 0) {
            throw new CoinAPIException(CURRENCY_NOT_FOUND_ERROR);
        }

        return resultArray[0];
    }

    public Map<String, Double> getSpecificCurrentPrices(Set<String> offeringCodes) {
        Map<String, Double> currencyPrices = new LinkedHashMap<>();
        for (String offeringCode : offeringCodes) {
            validateCurrencyIsUpToDate(offeringCode);

            double currencyPrice = getCoinCurrentPrice(offeringCode);
            currencyPrices.put(offeringCode, currencyPrice);
        }

        return currencyPrices;
    }
}
