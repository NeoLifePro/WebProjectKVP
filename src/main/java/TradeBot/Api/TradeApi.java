package TradeBot.Api;

import TradeBot.Auth.User;
import TradeBot.Auth.UserRepository;
import TradeBot.trade.TableInOrder.TradeDto;
import TradeBot.trade.TableInOrder.service.TradeService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/trades/")
public class TradeApi {

    private final TradeService tradeService;
    private final UserRepository userRepository;

    public TradeApi(TradeService tradeService, UserRepository userRepository) {
        this.tradeService = tradeService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public Page<TradeDto> getUserTrades(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {


        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }

        String username = auth.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        Long userId = user.getId();

        return tradeService.getTradesForUser(userId, page, size);
    }
}
