package TradeBot.client.order;

import TradeBot.client.ClientUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

public class OpenOrder {

    private final String apiKey;
    private final String secretKey;

    public OpenOrder(String apiKey, String secretKey) {
        this.apiKey = apiKey;
        this.secretKey = secretKey;
    }

    public String getOpenOrders(String symbol) throws IOException, URISyntaxException {
        long currentTime = System.currentTimeMillis();

        StringBuilder query = new StringBuilder("timestamp=").append(currentTime);
        if (symbol != null && !symbol.isBlank()) {
            query.append("&symbol=").append(symbol);
        }

        String signature = ClientUtil.calculateSignature(query.toString(), secretKey);
        if (signature == null) {
            throw new IllegalStateException("Не удалось посчитать подпись для запроса");
        }
        query.append("&signature=").append(signature);

        URI uri = new URI("https://api.binance.com/api/v3/openOrders?" + query);
        URL url = uri.toURL();

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("X-MBX-APIKEY", apiKey);

        int responseCode = connection.getResponseCode();

        InputStreamReader isr = (responseCode == HttpURLConnection.HTTP_OK)
                ? new InputStreamReader(connection.getInputStream())
                : new InputStreamReader(connection.getErrorStream());

        BufferedReader reader = new BufferedReader(isr);
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();
        connection.disconnect();

        if (responseCode != HttpURLConnection.HTTP_OK) {
            System.err.println("Ошибка получения открытых ордеров. Код: " + responseCode);
            System.err.println("Ответ: " + response);
        }

        return response.toString();
    }

    public void checkOpenOrders(List<String> symbols) throws IOException, URISyntaxException {
        for (String symbol : symbols) {
            String json = getOpenOrders(symbol);
            System.out.println("Open orders for " + symbol + ":");
            System.out.println(json);
        }
    }

    public int cancelOrder(String symbol, long orderId) throws IOException, URISyntaxException {
        long currentTime = System.currentTimeMillis();

        StringBuilder query = new StringBuilder()
                .append("symbol=").append(symbol)
                .append("&orderId=").append(orderId)
                .append("&timestamp=").append(currentTime);

        String signature = ClientUtil.calculateSignature(query.toString(), secretKey);
        if (signature == null) {
            throw new IllegalStateException("Не удалось посчитать подпись для отмены ордера");
        }
        query.append("&signature=").append(signature);

        URI uri = new URI("https://api.binance.com/api/v3/order?" + query);
        URL url = uri.toURL();

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("DELETE");
        connection.setRequestProperty("X-MBX-APIKEY", apiKey);

        int responseCode = connection.getResponseCode();

        InputStreamReader isr = (responseCode >= 200 && responseCode < 300)
                ? new InputStreamReader(connection.getInputStream())
                : new InputStreamReader(connection.getErrorStream());

        BufferedReader reader = new BufferedReader(isr);
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();
        connection.disconnect();

        System.out.println("Cancel order response (" + responseCode + "): " + response);
        return responseCode;
    }


    public int cancelAllOpenOrders(String symbol) throws IOException, URISyntaxException {
        long currentTime = System.currentTimeMillis();

        StringBuilder query = new StringBuilder()
                .append("symbol=").append(symbol)
                .append("&timestamp=").append(currentTime);

        String signature = ClientUtil.calculateSignature(query.toString(), secretKey);
        if (signature == null) {
            throw new IllegalStateException("Не удалось посчитать подпись для отмены всех ордеров");
        }
        query.append("&signature=").append(signature);

        URI uri = new URI("https://api.binance.com/api/v3/openOrders?" + query);
        URL url = uri.toURL();

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("DELETE");
        connection.setRequestProperty("X-MBX-APIKEY", apiKey);

        int responseCode = connection.getResponseCode();

        InputStreamReader isr = (responseCode >= 200 && responseCode < 300)
                ? new InputStreamReader(connection.getInputStream())
                : new InputStreamReader(connection.getErrorStream());

        BufferedReader reader = new BufferedReader(isr);
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();
        connection.disconnect();

        System.out.println("Cancel ALL orders response (" + responseCode + "): " + response);
        return responseCode;
    }
}
