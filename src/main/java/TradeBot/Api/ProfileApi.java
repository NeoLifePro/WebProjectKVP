package TradeBot.Api;

import TradeBot.Auth.UserRepository;
import TradeBot.Auth.UserService;
import TradeBot.profile.ChangePasswordDto;
import TradeBot.profile.ProfileDetailsDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/profile")
public class ProfileApi {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/details")
    public ProfileDetailsDto getUserDetails() {
        return userService.getUserDetails();
    }

    @PostMapping("/change-password")
    public String changePassword(@RequestBody ChangePasswordDto changePasswordDto) {
        if (!userService.checkCurrentPassword(changePasswordDto.getCurrentPassword())) {
            return "Current password is incorrect";
        }
        userService.updatePassword(changePasswordDto.getNewPassword());
        return "Password updated successfully";
    }

    @GetMapping("/me")
    public Map<String, Object> me(Authentication auth) {
        var u = userRepository.findByUsername(auth.getName()).orElseThrow();
        return Map.of(
                "username", u.getUsername(),
                "email", u.getEmail(),
                "lastLoginAt", u.getLastLoginAt()
        );
    }
}
