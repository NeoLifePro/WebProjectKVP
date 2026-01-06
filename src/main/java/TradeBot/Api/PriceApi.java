package TradeBot.Api;


import TradeBot.client.Client;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/price/")
public class PriceApi {

    private Client buildClient(){
        String api_key = null;
        String api_secret = null;

        return new Client(api_key, api_secret);
    }

    @GetMapping
    public double getPrice(@RequestParam("symbol") String symbol) {

        Client client = buildClient();
        if(symbol.isBlank()){
            return -1;
        }
        String Symbol = symbol.toUpperCase().trim();

        return client.GetCryptoPrice(Symbol);
    }
}
