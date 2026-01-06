package TradeBot.Auth;

import org.springframework.security.core.GrantedAuthority;          // Интерфейс «полномочия» (роль/привилегия)
import org.springframework.security.core.authority.SimpleGrantedAuthority; // Простая реализация GrantedAuthority по строке (например, "ROLE_USER")
import org.springframework.security.core.userdetails.UserDetails;   // Контракт, который Spring Security использует как «пользователь в системе»
import org.springframework.security.core.userdetails.UserDetailsService; // Сервис, который возвращает UserDetails по логину/емейлу
import org.springframework.security.core.userdetails.UsernameNotFoundException; // Исключение, если пользователь не найден
import org.springframework.stereotype.Service;                      // Пометка бина-сервиса для контейнера Spring

/* ===== Стандартные утилиты Java для коллекций и потоков ===== */
import java.util.Set;
import java.util.stream.Collectors;


@Service
public class AppUserDetailsService implements UserDetailsService {


    private final UserRepository users;


    public AppUserDetailsService(UserRepository users){
        this.users = users;
    }

    @Override
    public UserDetails loadUserByUsername(String input) throws UsernameNotFoundException {

        User user = users.findByUsernameOrEmail(input, input)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));


        Set<GrantedAuthority> authorities = user.getRoles().stream()
                .map(r -> new SimpleGrantedAuthority(r.getName()))
                .collect(Collectors.toSet());


        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(!user.isEnabled())
                .build();
    }
}
