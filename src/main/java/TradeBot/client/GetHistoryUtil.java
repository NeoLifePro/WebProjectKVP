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

public class GetHistoryUtil {

    // Metode, lai veiktu API pieprasījumu uz Binance, iegūstot vēsturiskos cenas datus
    public static String makeApiRequest(String cryptoName, String interval, String apiKey) {
        try {
            long endTime = Instant.now().toEpochMilli(); // Iegūst pašreizējo laiku milisekundēs
            long startTime = endTime - (8 * 60 * 60 * 1000); // 8 stundas atpakaļ, milisekundēs
            String endpoint = String.format("https://api.binance.com/api/v1/klines?symbol=%s&interval=%s&startTime=%s&endTime=%s",
                    cryptoName, interval, startTime, endTime); // Izveido API pieprasījuma URL

            HttpURLConnection connection = createConnection(endpoint, apiKey); // Izveido HTTP savienojumu
            int responseCode = connection.getResponseCode(); // Iegūst HTTP atbildes kodu

            // Ja atbilde ir OK (200), nolasām un atgriežam atbildi
            if (responseCode == HttpURLConnection.HTTP_OK) {
                return readResponse(connection); // Nolasām atbildi no savienojuma
            } else {
                System.err.println("HTTP pieprasījuma kļūda. Atbildes kods: " + responseCode); // Ja atbilde nav OK, ziņojam par kļūdu
            }
        } catch (Exception e) {
            Logger.getLogger(GetPriceHistory.class.getName()).log(Level.SEVERE, "Kļūda API pieprasījumā", e); // Ielogē jebkādas izņēmuma kļūdas
        }
        return null; // Atgriež null, ja ir kļūda
    }

    // Metode, lai izveidotu HTTP savienojumu ar norādīto endpoint un API atslēgu
    public static HttpURLConnection createConnection(String endpoint, String apiKey) throws IOException, URISyntaxException {
        URI uri = new URI(endpoint); // Pārvērš endpoint uz URI
        URL url = uri.toURL(); // Pārvērš URI uz URL
        HttpURLConnection connection = (HttpURLConnection) url.openConnection(); // Izveido HTTP savienojumu
        connection.setRequestProperty("X-MBX-APIKEY", apiKey); // Iestata API atslēgu pieprasījuma galvenē
        connection.setRequestMethod("GET"); // Iestata pieprasījuma metodi uz GET
        return connection; // Atgriež izveidoto savienojumu
    }

    // Metode, lai izvilktu cenu datus no JSON atbildes
    public static Vector<Double> extractPriceData(String jsonResponse) {
        Vector<Double> prices = new Vector<>(); // Izveido vektoru, lai saglabātu cenas
        String[] candleData = jsonResponse.substring(1, jsonResponse.length() - 1).split("],\\["); // Sadala JSON atbildi uz sveciņām (klīnēm)

        // Caurstaigā katru sveci un iegūst cenu (5. lauks katrā datu rindā)
        for (String data : candleData) {
            String[] fields = data.split(",");
            if (fields.length >= 5) { // Pārliecināmies, ka ir vismaz 5 lauki (cena ir 5. lauks)
                double price = Double.parseDouble(fields[4].replace("\"", "")); // Iegūst cenu un noņem liekās pēdiņas
                prices.add(price); // Pievieno cenu vektoram
            }
        }
        return prices;
    }

    // Metode, lai izvilktu apjoma datus no JSON atbildes
    public static Vector<Double> extractVolumeData(String jsonResponse) {
        Vector<Double> volume = new Vector<>(); // Izveido vektoru, lai saglabātu apjoma datus
        String[] candleData = jsonResponse.substring(1, jsonResponse.length() - 1).split("],\\["); // Sadala JSON atbildi uz sveciņām (klīnēm)

        // Caurstaigā katru sveci un iegūst apjomu (6. lauks katrā datu rindā)
        for (String data : candleData) {
            String[] fields = data.split(",");
            if (fields.length >= 5) { // Pārliecināmies, ka ir vismaz 5 lauki (apjoms ir 6. lauks)
                double vol = Double.parseDouble(fields[5].replace("\"", "")); // Iegūst apjomu un noņem liekās pēdiņas
                volume.add(vol); // Pievieno apjomu vektoram
            }
        }
        return volume;
    }
}
