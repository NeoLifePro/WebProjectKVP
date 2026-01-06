package TradeBot.trade.chart;// package TradeBot.Market;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
public class ChartService {

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String BINANCE_URL = "https://api.binance.com/api/v3/klines";

    public List<CandleDto> getCandles(String symbol, String interval, int limit) {
        String url = BINANCE_URL +
                "?symbol=" + symbol +
                "&interval=" + interval +
                "&limit=" + limit;

        // Iegūstam “raw” masīvu no Binance API: katra rinda ir kline masīvs ar laukiem pēc indeksiem
        Object[][] raw = restTemplate.getForObject(url, Object[][].class);

        // Sagatavojam rezultāta sarakstu
        List<CandleDto> result = new ArrayList<>();

        // Ja API atgrieza null (piem., kļūda vai nav datu), atgriežam tukšu sarakstu
        if (raw == null) return result;

        // Pārveidojam katru “kline” uz mūsu DTO (ņemot tikai time/open/high/low/close)
        for (Object[] kline : raw) {
            // kline:
            // 0 openTime
            // 1 open
            // 2 high
            // 3 low
            // 4 close
            long openTimeMs = ((Number) kline[0]).longValue();
            double open = Double.parseDouble((String) kline[1]);
            double high = Double.parseDouble((String) kline[2]);
            double low  = Double.parseDouble((String) kline[3]);
            double close= Double.parseDouble((String) kline[4]);

            // Binance dod laiku milisekundēs — pārvēršam uz sekundēm (bieži vajag chart bibliotēkām)
            long timeSec = openTimeMs / 1000L;

            // Pievienojam jaunu sveci rezultātu sarakstam
            result.add(new CandleDto(timeSec, open, high, low, close));
        }

        return result;
    }
}
