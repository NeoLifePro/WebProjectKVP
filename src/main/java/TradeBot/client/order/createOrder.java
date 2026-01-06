package TradeBot.client.order;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static TradeBot.client.ClientUtil.buildQueryString;
import static TradeBot.client.ClientUtil.bytesToHex;
public class createOrder {
    String apiKey, secretKey;

    public createOrder(String apiKey, String secretKey) {
        this.apiKey = apiKey;
        this.secretKey = secretKey;
    }

    private String calculateSignature(String queryString) {
        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(this.secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256_HMAC.init(secretKeySpec);
            byte[] signatureBytes = sha256_HMAC.doFinal(queryString.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(signatureBytes);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            Logger.getLogger(createOrder.class.getName()).log(Level.SEVERE, "Error calculating signature", e);
            return null;
        }
    }

    public int createLimitOrder(String cryptoName, String side, String type, String qty, String price) {
        String url = "https://api.binance.com/api/v3/order";
        long timestamp = System.currentTimeMillis();

        // Create parameters for creating an order
        Map<String, String> params = new HashMap<>();
        params.put("symbol", cryptoName + "USDC");
        params.put("side", side);
        params.put("type", type);
        params.put("timeInForce", "GTC");
        params.put("quantity", qty);
        params.put("price", price);
        params.put("timestamp", String.valueOf(timestamp));

        // Calculate the HMAC signature
        String queryString = buildQueryString(params);
        String signature = calculateSignature(queryString);
        if (signature == null) return -1;
        params.put("signature", signature);

        return sendRequest(url, params);
    }

    public int createOrderMarket(String cryptoName, String side, String type, String qty) {
        String url = "https://api.binance.com/api/v3/order";
        long timestamp = System.currentTimeMillis();


        Map<String, String> params = new HashMap<>();
        params.put("symbol", cryptoName);
        params.put("side", side);
        params.put("type", type);
        params.put("quantity", qty);
        params.put("timestamp", String.valueOf(timestamp));


        String queryString = buildQueryString(params);
        String signature = calculateSignature(queryString);
        if (signature == null) return -1;
        params.put("signature", signature);

        return sendRequest(url, params);
    }

    public int createOcoOrder(String cryptoName, String side, String qty, String price, String stopPrice, String stopLimitPrice) {
        String url = "https://api.binance.com/api/v3/order/oco";
        long timestamp = System.currentTimeMillis();


        Map<String, String> params = new HashMap<>();
        params.put("symbol", cryptoName + "USDT");
        params.put("side", side);
        params.put("quantity", qty);
        params.put("price", price);
        params.put("stopPrice", stopPrice);
        params.put("stopLimitPrice", stopLimitPrice);
        params.put("timestamp", String.valueOf(timestamp));


        params.put("stopLimitTimeInForce", "GTC");


        String queryString = buildQueryString(params);
        String signature = calculateSignature(queryString);
        if (signature == null) return -1;
        params.put("signature", signature);

        return sendRequest(url, params);
    }


    private int sendRequest(String url, Map<String, String> params) {
        // Create request headers
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("X-MBX-APIKEY", apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(buildQueryString(params)))
                .build();

        // Send the POST request
        HttpClient httpClient = HttpClient.newHttpClient();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());


            System.out.println("Response Code: " + response.statusCode());
            System.out.println("Response Body: " + response.body());
            return response.statusCode();
        } catch (Exception e) {
            Logger.getLogger(createOrder.class.getName()).log(Level.SEVERE, "Error sending request", e);
            return -1;
        }
    }
}

