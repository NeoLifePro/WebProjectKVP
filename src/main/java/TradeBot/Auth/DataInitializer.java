package TradeBot.Auth;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;


@Component
public class DataInitializer implements CommandLineRunner {
    private final RoleRepository roles;
    private final UserRepository users;
    private final PasswordEncoder encoder;


    public DataInitializer(RoleRepository roles, UserRepository users, PasswordEncoder encoder){
        this.roles = roles; this.users = users; this.encoder = encoder;
    }


    @Override
    public void run(String... args) {
        roles.findByName("ROLE_USER").orElseGet(() -> roles.save(new Role("ROLE_USER")));
        Role adminRole = roles.findByName("ROLE_ADMIN").orElseGet(() -> roles.save(new Role("ROLE_ADMIN")));



        users.findByUsername("admin").orElseGet(() -> {
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@local");
            admin.setPassword(encoder.encode("admin123"));
            admin.getRoles().add(adminRole);
            return users.save(admin);
        });
    }
}
