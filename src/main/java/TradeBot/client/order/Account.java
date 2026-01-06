package TradeBot.client.order;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static TradeBot.client.ClientUtil.buildQueryString;
import static TradeBot.client.ClientUtil.bytesToHex;

public class Account {

    String apiKey, secretKey;
    TradeBot.client.order.GetPriceHistory getPrice;

    public Account(String apiKey, String secretKey){
        this.apiKey = apiKey;
        this.secretKey = secretKey;
        this.getPrice = new GetPriceHistory(apiKey);
    }

    public static class AssetBalance {
        private final double free;
        private final double locked;

        public AssetBalance(double free, double locked) {
            this.free = free;
            this.locked = locked;
        }

        public double getFree() {
            return free;
        }

        public double getLocked() {
            return locked;
        }

        public double getTotal() {
            return free + locked;
        }
    }


    public Map<String, AssetBalance> getBalances(String... assetsFilter) {
        Map<String, AssetBalance> balancesMap = new HashMap<>();
        if(apiKey == null || secretKey == null){
            return balancesMap;
        }
        String url = "https://api.binance.com/api/v3/account";
        long timestamp = System.currentTimeMillis();

        Map<String, String> params = new HashMap<>();
        params.put("timestamp", String.valueOf(timestamp));

        String queryString = buildQueryString(params);
        String signature;
        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key =
                    new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256_HMAC.init(secret_key);
            byte[] signatureBytes = sha256_HMAC.doFinal(queryString.getBytes(StandardCharsets.UTF_8));
            signature = bytesToHex(signatureBytes);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            Logger.getLogger(Account.class.getName())
                    .log(Level.SEVERE, "Error calculating signature", e);
            return balancesMap;
        }

        params.put("signature", signature);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url + "?" + buildQueryString(params)))
                .header("X-MBX-APIKEY", apiKey)
                .GET()
                .build();

        HttpClient httpClient = HttpClient.newHttpClient();
        try {
            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                System.out.println("Error: Received status code " + response.statusCode());
                System.out.println("Response Body: " + response.body());
                return balancesMap;
            }

            JSONObject jObject = new JSONObject(response.body());
            if (!jObject.has("balances")) {
                System.out.println("Error: 'balances' field not found.");
                System.out.println("Response Body: " + response.body());
                return balancesMap;
            }

            JSONArray balances = jObject.getJSONArray("balances");

            Set<String> filter = null;
            if (assetsFilter != null && assetsFilter.length > 0) {
                filter = Arrays.stream(assetsFilter)
                        .filter(Objects::nonNull)
                        .map(String::toUpperCase)
                        .collect(Collectors.toSet());
            }

            for (int i = 0; i < balances.length(); i++) {
                JSONObject balance = balances.getJSONObject(i);
                String asset = balance.getString("asset");

                if (filter != null && !filter.contains(asset.toUpperCase())) {
                    continue;
                }

                double free = balance.getDouble("free");
                double locked = balance.getDouble("locked");


                if (free == 0.0 && locked == 0.0) continue;

                balancesMap.put(asset, new AssetBalance(free, locked));
            }

        } catch (Exception e) {
            Logger.getLogger(Account.class.getName())
                    .log(Level.SEVERE, "Error processing response", e);
        }

        return balancesMap;
    }


    public Map<String, Double> getAccountBalanceV2() {

        String[] defaultAssets = {"BTC", "ETH", "TRX", "DOGE", "PEPE"};
        Map<String, AssetBalance> full = getBalances(defaultAssets);

        Map<String, Double> totals = new HashMap<>();
        for (Map.Entry<String, AssetBalance> e : full.entrySet()) {
            totals.put(e.getKey(), e.getValue().getTotal());
        }
        return totals;
    }


    public void getAccountBalance() {
        try {
            Map<String, AssetBalance> balances = getBalances("BTC", "ETH", "TRX", "DOGE", "PEPE");
            for (Map.Entry<String, AssetBalance> entry : balances.entrySet()) {
                String asset = entry.getKey();
                AssetBalance b = entry.getValue();

                double priceUSDT = getPrice.GetCryptoPrice(asset + "USDT");
                double totalInUSDT = b.getTotal() * priceUSDT;

                String formatted = String.format("%.5f", totalInUSDT);

                System.out.println("Актив: " + asset
                        + " free: " + b.getFree()
                        + " locked: " + b.getLocked()
                        + " ≈ " + formatted + " USDT");
            }
        } catch (Exception e) {
            Logger.getLogger(Account.class.getName())
                    .log(Level.SEVERE, "Error printing balances", e);
        }
    }

}
