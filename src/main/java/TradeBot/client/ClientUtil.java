package TradeBot.client;

import TradeBot.client.order.createOrder;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ClientUtil {

    // Metode, kas veido vaicājuma virkni no parametrā ievadītā Map
    public static String buildQueryString(Map<String, String> params) {
        return params.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue()) // Pārveido katru Map ierakstu uz "key=value"
                .collect(Collectors.joining("&")); // Apvieno visus "key=value" pārus, atdalot ar "&"
    }

    // Metode, kas pārveido baitus (bytes) uz heksadecimālu virkni
    public static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder(2 * bytes.length); // StringBuilder ar pietiekamu izmēru
        for (byte b : bytes) {
            hexString.append(String.format("%02x", b)); // Katru byte pārveido uz divu ciparu heksadecimālo formātu
        }
        return hexString.toString();
    }

    // Metode, kas aprēķina HMAC-SHA256 parakstu no vaicājuma virknes un slepenā atslēga
    public static String calculateSignature(String queryString, String secretKey) {
        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256"); // Izveido HMAC ar SHA-256 algoritmu
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256"); // Izveido noslēpumtaustiņu
            sha256_HMAC.init(secretKeySpec); // Inicializē HMAC ar noslēpumtaustiņu
            byte[] signatureBytes = sha256_HMAC.doFinal(queryString.getBytes(StandardCharsets.UTF_8)); // Aprēķina parakstu
            return bytesToHex(signatureBytes); // Atgriež parakstu heksadecimālajā formātā
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            Logger.getLogger(createOrder.class.getName()).log(Level.SEVERE, "Error calculating signature", e); // Logē kļūdu
            return null;
        }
    }

    // Metode, kas nolasīs HTTP atbildi no savienojuma un atgriezīs to kā virkni
    public static String readResponse(HttpURLConnection connection) throws IOException {
        InputStream inputStream = connection.getInputStream(); // Iegūst atbildes straumi
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream)); // Izveido lasītāju
        StringBuilder response = new StringBuilder(); // StringBuilder, lai uzkrātu atbildes datus
        String line;
        while ((line = reader.readLine()) != null) { // Lasīšana līdz beigu rindai
            response.append(line); // Pievieno katru rindu atbildē
        }
        reader.close(); // Aizver lasītāju
        return response.toString(); // Atgriež pilnu atbildi kā virkni
    }
}
