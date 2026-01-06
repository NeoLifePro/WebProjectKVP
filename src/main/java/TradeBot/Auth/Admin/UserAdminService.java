package TradeBot.Auth.Admin;

import TradeBot.Auth.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class UserAdminService {
    private final UserRepository users;
    private final RoleRepository roles;

    public UserAdminService(UserRepository users, RoleRepository roles) {
        this.users = users; this.roles = roles;
    }

    @Transactional(readOnly = true)
    public Optional<UserDetailsDto> details(Long id){
        return users.findById(id).map(u -> new UserDetailsDto(
                u.getId(), u.getUsername(), u.getEmail(), u.isEnabled(),
                u.getRoles().stream().map(Role::getName).toList(),
                u.getCreatedAt(), u.getLastLoginAt()
        ));
    }

    @Transactional
    public void setEnabled(Long userId, boolean enabled, String acting){
        User u = users.findById(userId).orElseThrow();
        if (u.getUsername().equals(acting))
            throw new IllegalArgumentException("nedrikst bloķēt sevi...");
        u.setEnabled(enabled);
        users.save(u);
    }

    @Transactional
    public void setRoles(Long userId, List<String> roleNames, String acting){
        User u = users.findById(userId).orElseThrow();
        Set<String> names = new HashSet<>(Optional.ofNullable(roleNames).orElseGet(List::of));
        if (u.getUsername().equals(acting) && !names.contains("ROLE_ADMIN"))
            throw new IllegalArgumentException("nedrikts noņemt sev Admin");
        if (names.isEmpty()) throw new IllegalArgumentException("Nepieceišama vismaz viena piekļuve(ROLE)");

        Set<Role> newRoles = new HashSet<>();
        for (String n : names) {
            Role r = roles.findByName(n).orElseGet(() -> roles.save(new Role(n)));
            newRoles.add(r);
        }
        u.setRoles(newRoles);
        users.save(u);
    }
}
