package TradeBot.Auth;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class CurrentUserAdvice {
    private final UserRepository users;

    public CurrentUserAdvice(UserRepository users) { this.users = users; }

    @ModelAttribute("currentUser")
    public User currentUser(org.springframework.security.core.Authentication auth) {
        if (auth == null) return null;
        return users.findByUsername(auth.getName()).orElse(null);
    }
}
