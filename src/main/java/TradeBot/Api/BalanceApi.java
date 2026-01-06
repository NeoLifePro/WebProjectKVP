package TradeBot.Api;

import TradeBot.client.Client;
import TradeBot.client.order.Account;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/balance")
public class BalanceApi {

    private String normalizeSymbol(String symbol){
        if (symbol == null || symbol.isBlank()) {
            return "ETH";
        }
        return symbol.toUpperCase();
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


    public static class BalanceSide {
        public String asset;
        public double free;
        public double locked;
        public double total;
    }


    public static class BalanceResponse {
        public BalanceSide fiat;
        public BalanceSide crypto;
    }

    @PostMapping(
            path = "/balance",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public BalanceResponse balance(@RequestBody Request req){

        Client client = buildClient(req);

        String cryptoAsset = normalizeSymbol(req.getSymbol()); // ETH/BTC/LTC/DOGE


        Map<String, Account.AssetBalance> balances =
                client.GetAccountBalances("USDC", cryptoAsset);

        BalanceResponse resp = new BalanceResponse();

        // ===== FIAT (USDC) =====
        BalanceSide fiat = new BalanceSide();
        fiat.asset = "USDC";
        Account.AssetBalance usdc = balances.get("USDC");
        if (usdc != null) {
            fiat.free = usdc.getFree();
            fiat.locked = usdc.getLocked();
            fiat.total = usdc.getTotal();
        }
        resp.fiat = fiat;

        BalanceSide crypto = new BalanceSide();
        crypto.asset = cryptoAsset;
        Account.AssetBalance coin = balances.get(cryptoAsset);
        if (coin != null) {
            crypto.free = coin.getFree();
            crypto.locked = coin.getLocked();
            crypto.total = coin.getTotal();
        }
        resp.crypto = crypto;

        /*System.out.println("[BalanceApi] symbol=" + cryptoAsset +
                " USDC=" + (usdc != null ? usdc.getTotal() : 0) +
                " CRYPTO=" + (coin != null ? coin.getTotal() : 0));
        */
        return resp;
    }

    @GetMapping(path = "/balance", produces = MediaType.TEXT_PLAIN_VALUE)
    public String balanceGetInfo() {
        return "Use POST /api/balance/balance with JSON body {api_key, api_secret, symbol}";
    }
}
