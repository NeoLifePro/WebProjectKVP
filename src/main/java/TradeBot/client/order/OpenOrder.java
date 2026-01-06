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

    // Konstruktors, kas iestata API atslēgu un slepeno atslēgu
    public OpenOrder(String apiKey, String secretKey) {
        this.apiKey = apiKey;
        this.secretKey = secretKey;
    }

    // Metode, lai iegūtu atvērtos darījumus no Binance API
    public String getOpenOrders(String symbol) throws IOException, URISyntaxException {
        long currentTime = System.currentTimeMillis(); // Pašreizējais laiks (timestamp)

        // Veido pieprasījuma virkni
        StringBuilder query = new StringBuilder("timestamp=").append(currentTime);
        if (symbol != null && !symbol.isBlank()) {
            query.append("&symbol=").append(symbol); // Ja ir simbols, pievieno to pieprasījumam
        }

        // Aprēķina parakstu
        String signature = ClientUtil.calculateSignature(query.toString(), secretKey);
        if (signature == null) {
            throw new IllegalStateException("Nevarēja aprēķināt parakstu");
        }
        query.append("&signature=").append(signature); // Pievieno parakstu pie pieprasījuma

        URI uri = new URI("https://api.binance.com/api/v3/openOrders?" + query); // Izveido URI
        URL url = uri.toURL(); // Pārvērš URI uz URL

        HttpURLConnection connection = (HttpURLConnection) url.openConnection(); // Izveido savienojumu
        connection.setRequestMethod("GET"); // Uzstāda pieprasījuma metodi uz GET
        connection.setRequestProperty("X-MBX-APIKEY", apiKey); // Pievieno API atslēgu pieprasījumā

        int responseCode = connection.getResponseCode(); // Iegūst atbildes kodu

        InputStreamReader isr = (responseCode == HttpURLConnection.HTTP_OK)
                ? new InputStreamReader(connection.getInputStream()) // Ja atbilde ir OK, lasām atbildi
                : new InputStreamReader(connection.getErrorStream()); // Ja ir kļūda, lasām kļūdas straumi

        BufferedReader reader = new BufferedReader(isr); // Izveido lasītāju
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line); // Pievieno katru rindu atbildē
        }
        reader.close(); // Aizver lasītāju
        connection.disconnect(); // Aizver savienojumu

        if (responseCode != HttpURLConnection.HTTP_OK) {
            System.err.println("Kļūda: " + responseCode);
            System.err.println("Atbilde: " + response);
        }

        return response.toString(); // Atgriež atbildi kā virkni
    }

    // Metode, lai pārbaudītu atvērtos darījumus visiem simboliem
    public void checkOpenOrders(List<String> symbols) throws IOException, URISyntaxException {
        for (String symbol : symbols) {
            String json = getOpenOrders(symbol); // Iegūst atvērtos darījumus katram simbolam
            System.out.println("Atvērtie darījumi simbolam " + symbol + ":");
            System.out.println(json); // Izvada atbildi
        }
    }

    // Metode, lai atceltu darījumu ar norādīto ID
    public int cancelOrder(String symbol, long orderId) throws IOException, URISyntaxException {
        long currentTime = System.currentTimeMillis(); // Pašreizējais laiks (timestamp)

        // Veido pieprasījuma virkni ar simbolu, darījuma ID un laiku
        StringBuilder query = new StringBuilder()
                .append("symbol=").append(symbol)
                .append("&orderId=").append(orderId)
                .append("&timestamp=").append(currentTime);

        // Aprēķina parakstu
        String signature = ClientUtil.calculateSignature(query.toString(), secretKey);
        if (signature == null) {
            throw new IllegalStateException("Nevarēja aprēķināt parakstu darījuma atcelšanai");
        }
        query.append("&signature=").append(signature); // Pievieno parakstu pie pieprasījuma

        URI uri = new URI("https://api.binance.com/api/v3/order?" + query); // Izveido URI
        URL url = uri.toURL(); // Pārvērš URI uz URL

        HttpURLConnection connection = (HttpURLConnection) url.openConnection(); // Izveido savienojumu
        connection.setRequestMethod("DELETE"); // Uzstāda pieprasījuma metodi uz DELETE
        connection.setRequestProperty("X-MBX-APIKEY", apiKey); // Pievieno API atslēgu pieprasījumā

        int responseCode = connection.getResponseCode(); // Iegūst atbildes kodu

        InputStreamReader isr = (responseCode >= 200 && responseCode < 300)
                ? new InputStreamReader(connection.getInputStream()) // Ja atbilde ir veiksmīga, lasām atbildi
                : new InputStreamReader(connection.getErrorStream()); // Ja ir kļūda, lasām kļūdas straumi

        BufferedReader reader = new BufferedReader(isr); // Izveido lasītāju
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line); // Pievieno katru rindu atbildē
        }
        reader.close(); // Aizver lasītāju
        connection.disconnect(); // Aizver savienojumu

        System.out.println("Atcelšanas atbilde (" + responseCode + "): " + response); // Izvada atbildi
        return responseCode; // Atgriež atbildes kodu
    }

    // Metode, lai atceltu visus atvērtos darījumus ar konkrēto simbolu
    public int cancelAllOpenOrders(String symbol) throws IOException, URISyntaxException {
        long currentTime = System.currentTimeMillis(); // Pašreizējais laiks (timestamp)

        // Veido pieprasījuma virkni ar simbolu un laiku
        StringBuilder query = new StringBuilder()
                .append("symbol=").append(symbol)
                .append("&timestamp=").append(currentTime);

        // Aprēķina parakstu
        String signature = ClientUtil.calculateSignature(query.toString(), secretKey);
        if (signature == null) {
            throw new IllegalStateException("Nevarēja aprēķināt parakstu visiem darījumiem");
        }
        query.append("&signature=").append(signature); // Pievieno parakstu pie pieprasījuma

        URI uri = new URI("https://api.binance.com/api/v3/openOrders?" + query); // Izveido URI
        URL url = uri.toURL(); // Pārvērš URI uz URL

        HttpURLConnection connection = (HttpURLConnection) url.openConnection(); // Izveido savienojumu
        connection.setRequestMethod("DELETE"); // Uzstāda pieprasījuma metodi uz DELETE
        connection.setRequestProperty("X-MBX-APIKEY", apiKey); // Pievieno API atslēgu pieprasījumā

        int responseCode = connection.getResponseCode(); // Iegūst atbildes kodu

        InputStreamReader isr = (responseCode >= 200 && responseCode < 300)
                ? new InputStreamReader(connection.getInputStream()) // Ja atbilde ir veiksmīga, lasām atbildi
                : new InputStreamReader(connection.getErrorStream()); // Ja ir kļūda, lasām kļūdas straumi

        BufferedReader reader = new BufferedReader(isr); // Izveido lasītāju
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line); // Pievieno katru rindu atbildē
        }
        reader.close(); // Aizver lasītāju
        connection.disconnect(); // Aizver savienojumu

        System.out.println("Atcelšanas visu darījumu atbilde (" + responseCode + "): " + response); // Izvada atbildi
        return responseCode; // Atgriež atbildes kodu
    }
}
