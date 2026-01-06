package TradeBot.Controller;

import TradeBot.Auth.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Controller
public class DashboardController {

    private final UserRepository userRepository;

    public DashboardController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication auth, Model model) {
        String username = auth.getName();

        var user = userRepository.findByUsername(username).orElse(null);

        String lastLogin = "—";
        if (user != null && user.getLastLoginAt() != null) {
            lastLogin = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                    .withZone(ZoneId.of("Europe/Riga"))
                    .format(user.getLastLoginAt());
        }

        model.addAttribute("lastLogin", lastLogin);
        return "dashboard";
    }
}
