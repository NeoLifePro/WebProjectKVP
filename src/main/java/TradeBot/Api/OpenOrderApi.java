package TradeBot.Api;
import java.time.LocalDateTime;
import TradeBot.client.Client;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/openOrder")
public class OpenOrderApi {

    private String normalizeSymbol(String symbol){
        if (symbol == null || symbol.isBlank()) return null;
        String s = symbol.toUpperCase();
        if (!s.endsWith("USDC") && !s.endsWith("USDT")) {
            s = s + "USDC";
        }
        return s;
    }

    private Client buildClient(Request req){
        String apiKey = (req.getApi_key() == null || req.getApi_key().isBlank())
                ? null
                : req.getApi_key().trim();
        String secretKey = (req.getApi_secret() == null || req.getApi_secret().isBlank())
                ? null
                : req.getApi_secret().trim();

        return new Client(apiKey, secretKey);
    }

    @PostMapping(
            value = "/opened",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public String openOrder(@RequestBody Request req){
        Client client = buildClient(req);
        String json = client.getOpenOrders(normalizeSymbol(req.getSymbol()));
        LocalDateTime time = LocalDateTime.now();
        System.out.println("[OpenOrderApi] "+ time  + json);
        return json;
    }


    @PostMapping(value = "/cancel", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void cancelStub(@RequestBody Request req) {
        Client client = buildClient(req);
        System.out.println(req.getOrderId());
        client.cancelOrder(req.getSymbol(), req.getOrderId());
    }


}
