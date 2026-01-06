package TradeBot.Api;

import TradeBot.Auth.User;
import TradeBot.Auth.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserPassApi {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminUserPassApi(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/{id}/password")
    public ResponseEntity<?> changeUserPassword(
            @PathVariable Long id,
            @RequestParam("newPassword") String newPassword
    ) {
        if (newPassword == null || newPassword.trim().length() < 4) {
            return ResponseEntity.badRequest().body("Password must be at least 4 characters.");
        }

        User u = userRepository.findById(id).orElse(null);
        if (u == null) {
            return ResponseEntity.notFound().build();
        }

        u.setPassword(passwordEncoder.encode(newPassword.trim()));
        userRepository.save(u);

        return ResponseEntity.ok("Password changed.");
    }
}
