package TradeBot.Auth.Admin;

import java.time.Instant;
import java.util.List;

public record UserDetailsDto(
        Long id,
        String username,
        String email,
        boolean enabled,
        List<String> roles,
        Instant createdAt,
        Instant lastLoginAt

) {}
