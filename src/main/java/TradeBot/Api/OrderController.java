package TradeBot.Api;

import TradeBot.client.Client;
import TradeBot.trade.TableInOrder.Trade;
import TradeBot.trade.TableInOrder.TradeRepository;
import TradeBot.Auth.User;
import TradeBot.Auth.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/order")
public class OrderController {

    private final TradeRepository tradeRepository;
    private final UserRepository userRepository;

    @Autowired
    public OrderController(TradeRepository tradeRepository,
                           UserRepository userRepository) {
        this.tradeRepository = tradeRepository;
        this.userRepository = userRepository;
    }

    private String normalizeSymbol(String rawSymbol) {
        if (rawSymbol == null || rawSymbol.isBlank()) {
            return "Symbol is Blank!";
        }
        return rawSymbol.trim().toUpperCase();
    }

    private Client buildClient(Request req) {
        String apiKey = (req.getApi_key() == null || req.getApi_key().isBlank())
                ? null
                : req.getApi_key().trim();

        String secret_key = (req.getApi_secret() == null || req.getApi_secret().isBlank())
                ? null
                : req.getApi_secret().trim();

        System.out.println("API_KEY = " + apiKey);
        System.out.println("SECRET_KEY = " + secret_key);

        return new Client(apiKey, secret_key);
    }

    private void saveTrade(String symbol,
                           String side,
                           String price,
                           String qty,
                           String status) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()
                || "anonymousUser".equals(auth.getName())) {
            System.out.println("No authenticated user -> trade not saved");
            return;
        }

        String username = auth.getName();

        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            System.out.println("User entity not found -> trade not saved");
            return;
        }

        Trade trade = new Trade();
        trade.setUser(user);
        trade.setSymbol(symbol);
        trade.setSide(side);

        try {
            trade.setPrice(new BigDecimal(price));
        } catch (Exception e) {
            trade.setPrice(BigDecimal.ZERO);
        }

        try {
            trade.setAmount(new BigDecimal(qty));
        } catch (Exception e) {
            trade.setAmount(BigDecimal.ZERO);
        }

        trade.setStatus(status);
        tradeRepository.save(trade);
    }

    // ==== LIMIT ORDER ====
    @PostMapping(
            path = "/limit",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.TEXT_PLAIN_VALUE
    )
    public void CreateLimitOrder(@RequestBody Request req) {

        String getSellPrice = (req.getPrice() == null ? "" : req.getPrice().trim());
        String QTY = (req.getQty() == null || req.getQty().isBlank() ? null : req.getQty());
        String SIDE = (req.getSide() == null || req.getSide().isBlank() ? null : req.getSide());

        if (getSellPrice.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Sell price is blank"
            );
        }

        String symbol = normalizeSymbol(req.getSymbol());
        Client client = buildClient(req);

        try {
            if(client.createLimitOrder(symbol, SIDE, "LIMIT", QTY, getSellPrice) == 200)
                saveTrade(symbol, SIDE, getSellPrice, QTY, "SUCCESS");
            else
                saveTrade(symbol, SIDE, getSellPrice, QTY, "ERROR");
        } catch (Exception e) {
            e.printStackTrace();

            // ЛОГИРУЕМ КАК ERROR
            saveTrade(symbol, SIDE, getSellPrice, QTY, "ERROR");

            // И говорим фронту, что это 400 (или 500 — как хочешь)
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Order creation failed: " + e.getMessage()
            );
        }
    }
    @PostMapping(
            path = "/market",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.TEXT_PLAIN_VALUE
    )
    public String createMarketOrder(@RequestBody Request req) {


        String symbol = normalizeSymbol(req.getSymbol());


        String SIDE = (req.getSide() == null || req.getSide().isBlank()) ? null : req.getSide();


        String QTY = (req.getQty() == null || req.getQty().isBlank()) ? null : req.getQty();


        Client client = buildClient(req);

        try {

            int code = client.createOrderMarket(symbol, SIDE, "MARKET", QTY);

            if (code == 200) {
                saveTrade(symbol, SIDE, "0", QTY, "SUCCESS");
                return "OK";
            } else {
                saveTrade(symbol, SIDE, "0", QTY, "ERROR");
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Market order creation failed");
            }

        } catch (Exception e) {
            saveTrade(symbol, SIDE, "0", QTY, "ERROR");
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Order creation failed: " + e.getMessage()
            );
        }
    }
}
