package TradeBot.Api;

import TradeBot.client.Client;
import TradeBot.client.crypto.Indicators;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Vector;

@RestController
@RequestMapping("/api/ind")
public class IndicatorsClientController {


    private String normalizeSymbol(String rawSymbol) {
        if (rawSymbol == null || rawSymbol.isBlank()) {
            return "BTCUSDT";
        }
        return rawSymbol.trim().toUpperCase();
    }

    private Client buildClient(Request req) {
        String apiKey = (req.getApi_key() == null || req.getApi_key().isBlank())
                ? null
                : req.getApi_key().trim();
        String secretKey = (req.getApi_secret() == null || req.getApi_secret().isBlank())
                ? null
                : req.getApi_secret().trim();


        return new Client(apiKey, secretKey);
    }


    private double[] prices15m(Client client, String symbol) {
        Vector<Double> pricesVec = client.GetCryptoPrice_15Min(symbol);
        if (pricesVec == null || pricesVec.isEmpty()) {
            return new double[0];
        }
        double[] arr = new double[pricesVec.size()];
        for (int i = 0; i < pricesVec.size(); i++) {
            arr[i] = pricesVec.get(i);
        }
        return arr;
    }


    private double lastValid(double[] arr) {
        if (arr == null || arr.length == 0) {
            return Double.NaN;
        }
        for (int i = arr.length - 1; i >= 0; i--) {
            if (!Double.isNaN(arr[i])) {
                return arr[i];
            }
        }
        return Double.NaN;
    }

    // ====== SMA(14) ======
    @PostMapping(path = "/sma14",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.TEXT_PLAIN_VALUE)
    public String sma14(@RequestBody Request req) {
        String symbol = normalizeSymbol(req.getSymbol());
        Client client = buildClient(req);
        double[] closes = prices15m(client, symbol);

        double[] smaArr = Indicators.sma(closes, 14);
        double v = lastValid(smaArr);
        return Double.toString(v);
    }

    // ====== EMA(14) ======
    @PostMapping(path = "/ema14",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.TEXT_PLAIN_VALUE)
    public String ema14(@RequestBody Request req) {
        String symbol = normalizeSymbol(req.getSymbol());
        Client client = buildClient(req);
        double[] closes = prices15m(client, symbol);

        double[] emaArr = Indicators.ema(closes, 14);
        double v = lastValid(emaArr);
        return Double.toString(v);
    }

    // ====== WMA(14) ======
    @PostMapping(path = "/wma14",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.TEXT_PLAIN_VALUE)
    public String wma14(@RequestBody Request req) {
        String symbol = normalizeSymbol(req.getSymbol());
        Client client = buildClient(req);
        double[] closes = prices15m(client, symbol);

        double[] wmaArr = Indicators.wma(closes, 14);
        double v = lastValid(wmaArr);
        return Double.toString(v);
    }

    // ====== ROC(10) ======
    @PostMapping(path = "/roc10",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.TEXT_PLAIN_VALUE)
    public String roc10(@RequestBody Request req) {
        String symbol = normalizeSymbol(req.getSymbol());
        Client client = buildClient(req);
        double[] closes = prices15m(client, symbol);

        double[] rocArr = Indicators.roc(closes, 10);
        double v = lastValid(rocArr);
        return Double.toString(v);
    }

    // ====== RSI(14) ======
    @PostMapping(path = "/rsi14",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.TEXT_PLAIN_VALUE)
    public String rsi14(@RequestBody Request req) {
        String symbol = normalizeSymbol(req.getSymbol());
        Client client = buildClient(req);
        double[] closes = prices15m(client, symbol);

        double[] rsiArr = Indicators.rsi(closes, 14);
        double v = lastValid(rsiArr);
        return Double.toString(v);
    }

    // ====== MACD line (12,26,9) ======
    @PostMapping(path = "/macdLine",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.TEXT_PLAIN_VALUE)
    public String macdLine(@RequestBody Request req) {
        String symbol = normalizeSymbol(req.getSymbol());
        Client client = buildClient(req);
        double[] closes = prices15m(client, symbol);

        Indicators.MacdResult macd = Indicators.macd(closes, 12, 26, 9);
        double v = lastValid(macd.macd);
        return Double.toString(v);
    }

    // ====== MACD signal (12,26,9) ======
    @PostMapping(path = "/macdSignal",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.TEXT_PLAIN_VALUE)
    public String macdSignal(@RequestBody Request req) {
        String symbol = normalizeSymbol(req.getSymbol());
        Client client = buildClient(req);
        double[] closes = prices15m(client, symbol);

        Indicators.MacdResult macd = Indicators.macd(closes, 12, 26, 9);
        double v = lastValid(macd.signal);
        return Double.toString(v);
    }

    // ====== MACD histogram (12,26,9) ======
    @PostMapping(path = "/macdHist",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.TEXT_PLAIN_VALUE)
    public String macdHistogram(@RequestBody Request req) {
        String symbol = normalizeSymbol(req.getSymbol());
        Client client = buildClient(req);
        double[] closes = prices15m(client, symbol);

        Indicators.MacdResult macd = Indicators.macd(closes, 12, 26, 9);
        double v = lastValid(macd.histogram);
        return Double.toString(v);
    }

    // ====== Bollinger middle(20,2) ======
    @PostMapping(path = "/bbMiddle",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.TEXT_PLAIN_VALUE)
    public String bbMiddle(@RequestBody Request req) {
        String symbol = normalizeSymbol(req.getSymbol());
        Client client = buildClient(req);
        double[] closes = prices15m(client, symbol);

        Indicators.BollingerBands bb = Indicators.bollinger(closes, 20, 2.0);
        double v = lastValid(bb.middle);
        return Double.toString(v);
    }

    // ====== Bollinger upper(20,2) ======
    @PostMapping(path = "/bbUpper",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.TEXT_PLAIN_VALUE)
    public String bbUpper(@RequestBody Request req) {
        String symbol = normalizeSymbol(req.getSymbol());
        Client client = buildClient(req);
        double[] closes = prices15m(client, symbol);

        Indicators.BollingerBands bb = Indicators.bollinger(closes, 20, 2.0);
        double v = lastValid(bb.upper);
        return Double.toString(v);
    }

    // ====== Bollinger lower(20,2) ======
    @PostMapping(path = "/bbLower",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.TEXT_PLAIN_VALUE)
    public String bbLower(@RequestBody Request req) {
        String symbol = normalizeSymbol(req.getSymbol());
        Client client = buildClient(req);
        double[] closes = prices15m(client, symbol);

        Indicators.BollingerBands bb = Indicators.bollinger(closes, 20, 2.0);
        double v = lastValid(bb.lower);
        return Double.toString(v);
    }

}
