package TradeBot.client.order;


import java.net.HttpURLConnection;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import static TradeBot.client.ClientUtil.readResponse;
import static TradeBot.client.GetHistoryUtil.*;

public class GetPriceHistory {
    final String apiKey;
    public GetPriceHistory(String apiKey){
        this.apiKey = apiKey;
    }

    public double GetCryptoPrice(String CryptoName) {
        double result = 0;
        try {
            //BTC TO USDT
            HttpURLConnection connection = createConnection("https://api.binance.com/api/v3/ticker/price?symbol=" + CryptoName, apiKey);

            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                String jsonResponse = readResponse(connection);
                String btcPrice = jsonResponse.split("\"price\":\"")[1].split("\"")[0];
                result = Double.parseDouble(btcPrice);
                //System.out.println("Цена BTC в USDT: " + btcPrice);
            } else {
                System.err.println("Ошибка HTTP-запроса. Код ответа: " + responseCode);
            }

            connection.disconnect();

        } catch (Exception e) {
            Logger.getLogger(createOrder.class.getName()).log(Level.SEVERE, "Error calculating signature", e);
        }
        return result;
    }
    public Vector<Double> GetCryptoPrice_1H(String CryptoName){
        return getCryptoPrice(CryptoName.toUpperCase(), "1h");
    }
    public Vector<Double> GetCryptoPrice_15Min(String CryptoName) {
        return getCryptoPrice(CryptoName.toUpperCase(), "15m");
    }
    public Vector<Double> GetCryptoPrice_5Min(String CryptoName) {
        return getCryptoPrice(CryptoName.toUpperCase(), "5m");
    }
    public Vector<Double> GetCryptoPrice_30Min(String CryptoName) {
        return getCryptoPrice(CryptoName.toUpperCase(), "30m");
    }
    private Vector<Double> getCryptoPrice(String cryptoName, String interval) {
        Vector<Double> prices = new Vector<>();
        String jsonResponse = makeApiRequest(cryptoName.toUpperCase(), interval, apiKey);
        if (jsonResponse != null) {
            prices = extractPriceData(jsonResponse);
        }
        return prices;
    }
}
