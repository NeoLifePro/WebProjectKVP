package TradeBot.Auth;

import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class LoginSuccessListener {

    private final UserRepository userRepository;

    public LoginSuccessListener(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @EventListener
    public void onLoginSuccess(AuthenticationSuccessEvent event) {
        String username = event.getAuthentication().getName();
        userRepository.updateLastLoginAt(username, Instant.now());
    }
}
