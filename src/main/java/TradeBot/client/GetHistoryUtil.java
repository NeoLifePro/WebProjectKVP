package TradeBot.client;

import TradeBot.client.order.GetPriceHistory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Instant;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import static TradeBot.client.ClientUtil.readResponse;

public class GetHistoryUtil{
    public static String makeApiRequest(String cryptoName, String interval, String apiKey) {
        try {
            long endTime = Instant.now().toEpochMilli();
            long startTime = endTime - (8 * 60 * 60 * 1000); // 8 hours in milliseconds
            String endpoint = String.format("https://api.binance.com/api/v1/klines?symbol=%s&interval=%s&startTime=%s&endTime=%s",
                    cryptoName, interval, startTime, endTime);

            HttpURLConnection connection = createConnection(endpoint, apiKey);
            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                return readResponse(connection);
            } else {
                System.err.println("HTTP request error. Response code: " + responseCode);
            }
        } catch (Exception e) {
            Logger.getLogger(GetPriceHistory.class.getName()).log(Level.SEVERE, "Error in API request", e);
        }
        return null;
    }

    public static HttpURLConnection createConnection(String endpoint, String apiKey) throws IOException, URISyntaxException {
        URI uri = new URI(endpoint);
        URL url = uri.toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("X-MBX-APIKEY", apiKey);
        connection.setRequestMethod("GET");
        return connection;
    }

    public static Vector<Double> extractPriceData(String jsonResponse) {
        Vector<Double> prices = new Vector<>();
        String[] candleData = jsonResponse.substring(1, jsonResponse.length() - 1).split("],\\[");
        for (String data : candleData) {
            String[] fields = data.split(",");
            if (fields.length >= 5) {
                double price = Double.parseDouble(fields[4].replace("\"", ""));
                prices.add(price);
            }
        }
        return prices;
    }
    public static Vector<Double> extractVolumeData(String jsonResponse) {
        Vector<Double> volume = new Vector<>();
        String[] candleData = jsonResponse.substring(1, jsonResponse.length() - 1).split("],\\[");
        for (String data : candleData) {
            String[] fields = data.split(",");
            if (fields.length >= 5) {
                double price = Double.parseDouble(fields[5].replace("\"", ""));
                volume.add(price);
            }
        }
        return volume;
    }
}