package TradeBot.client.order;

import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import static TradeBot.client.ClientUtil.readResponse;
import static TradeBot.client.GetHistoryUtil.*;


public class GetVolumeHistory {
    final String apiKey;
    public GetVolumeHistory(String apiKey){
        this.apiKey = apiKey;
    }
    public double GetCryptoVolume(String CryptoName) {
        double result = 0;
        try {
            HttpURLConnection connection = createConnection("https://api.binance.com/api/v3/ticker/24hr?symbol=" + CryptoName.toUpperCase(),apiKey);

            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                String jsonResponse = readResponse(connection);
                JSONObject jsonObj = new JSONObject(jsonResponse);
                result = jsonObj.getDouble("volume");
            } else {
                System.err.println("Ошибка HTTP-запроса. Код ответа: " + responseCode);
            }

            connection.disconnect();

        } catch (Exception e) {
            Logger.getLogger(GetPriceHistory.class.getName()).log(Level.SEVERE, "Error in GetCryptoVolume", e);
        }
        return result;
    }
    public Vector<Double> GetCryptoVolume_30Min(String cryptoName){
        return getCryptoVolume(cryptoName.toUpperCase(), "30m");
    }
    public Vector<Double> GetCryptoVolume_15Min(String cryptoName){
        return getCryptoVolume(cryptoName.toUpperCase(), "15m");
    }
    private Vector<Double> getCryptoVolume(String cryptoName, String interval){
        Vector<Double> volume = new Vector<>();
        String jsonResponse = makeApiRequest(cryptoName.toUpperCase(), interval, apiKey);
        if (jsonResponse != null) {
            volume = extractVolumeData(jsonResponse);
        }
        return volume;
    }
}
