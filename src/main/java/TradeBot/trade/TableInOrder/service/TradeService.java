package TradeBot.trade.TableInOrder.service;

import TradeBot.Auth.User;
import TradeBot.trade.TableInOrder.Trade;
import TradeBot.trade.TableInOrder.TradeDto;
import TradeBot.trade.TableInOrder.TradeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class TradeService {

    private final TradeRepository tradeRepository;

    public TradeService(TradeRepository tradeRepository) {
        this.tradeRepository = tradeRepository;
    }


    public Page<TradeDto> getTradesForUser(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        Page<Trade> trades = tradeRepository
                .findByUserIdOrderByCreatedAtDesc(userId, pageable);

        return trades.map(t ->
                new TradeDto(
                        t.getCreatedAt(),   // time
                        t.getSymbol(),      // pair
                        t.getSide(),        // side
                        t.getPrice(),
                        t.getAmount(),
                        t.getStatus()
                )
        );
    }

    public void logTrade(Long userId,
                         String symbol,
                         String side,
                         String status,
                         BigDecimal price,
                         BigDecimal amount) {

        Trade trade = new Trade();


        User user = new User();
        user.setId(userId);
        trade.setUser(user);

        trade.setSymbol(symbol);
        trade.setSide(side);
        trade.setStatus(status);
        trade.setPrice(price);
        trade.setAmount(amount);

        tradeRepository.save(trade);
    }
}
