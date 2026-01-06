package TradeBot.Auth;

import TradeBot.profile.ProfileDetailsDto;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class UserService {
    private final UserRepository users;
    private final RoleRepository roles;
    private final PasswordEncoder encoder;

    public UserService(UserRepository users, RoleRepository roles, PasswordEncoder encoder){
        this.users = users;
        this.roles = roles;
        this.encoder = encoder;
    }

    @Transactional
    public User register(RegisterDto dto){
        if (users.existsByUsername(dto.getUsername()))
            throw new IllegalArgumentException("Username already taken");
        if (users.existsByEmail(dto.getEmail()))
            throw new IllegalArgumentException("Email already in use");

        User u = new User();
        u.setUsername(dto.getUsername().trim());
        u.setEmail(dto.getEmail().trim().toLowerCase());
        u.setPassword(encoder.encode(dto.getPassword()));
        u.getRoles().add(
                roles.findByName("ROLE_USER").orElseGet(() -> roles.save(new Role("ROLE_USER")))
        );
        return users.save(u);
    }

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;


    public boolean checkCurrentPassword(String currentPassword) {
        User currentUser = getCurrentUser();
        return passwordEncoder.matches(currentPassword, currentUser.getPassword());
    }

    public void updatePassword(String newPassword) {

        User currentUser = getCurrentUser();


        currentUser.setPassword(passwordEncoder.encode(newPassword));


        userRepository.save(currentUser);
    }


    public ProfileDetailsDto getUserDetails() {

        User currentUser = getCurrentUser();
        return new ProfileDetailsDto(currentUser.getUsername(), currentUser.getEmail());
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }
}
