package bg.sofia.uni.fmi.mjt.crypto.wallet.coinAPI;

public class CurrencyDTO implements Comparable<CurrencyDTO> {
    private String asset_id;
    private int type_is_crypto;
    private double price_usd;

    public CurrencyDTO(String assetId, int typeIsCrypto, double priceUSD) {
        asset_id = assetId;
        type_is_crypto = typeIsCrypto;
        price_usd = priceUSD;
    }

    public boolean isCrypto() {
        return type_is_crypto == 1;
    }

    public boolean hasValidPrice() {
        return price_usd != 0;
    }

    public String getAssetId() {
        return asset_id;
    }

    public double getPriceUSD() {
        return price_usd;
    }

    @Override
    public int compareTo(CurrencyDTO otherCoin) {
        return Double.compare(price_usd, otherCoin.price_usd);
    }
}
