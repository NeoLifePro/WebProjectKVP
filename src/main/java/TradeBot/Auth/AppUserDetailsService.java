package TradeBot.Auth;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


import java.util.Set;
import java.util.stream.Collectors;


@Service
public class AppUserDetailsService implements UserDetailsService {

    private final UserRepository users; // Repozitorijs, kas meklē lietotājus datubāzē

    // Konstruktorā tiek ievadīts lietotāju repozitorijs
    public AppUserDetailsService(UserRepository users){
        this.users = users;
    }

    // Metode, kas tiek izsaukta, lai iegūtu lietotāja datus pēc lietotāja vārda vai e-pasta
    @Override
    public UserDetails loadUserByUsername(String input) throws UsernameNotFoundException {

        // Mēģina atrast lietotāju pēc lietotāja vārda vai e-pasta
        User user = users.findByUsernameOrEmail(input, input)
                .orElseThrow(() -> new UsernameNotFoundException("User not found")); // Ja lietotājs netiek atrasts, tiek izmests izņēmums

        // Pārveido lietotāja lomas uz Spring Security autoritātēm (roles)
        Set<GrantedAuthority> authorities = user.getRoles().stream()
                .map(r -> new SimpleGrantedAuthority(r.getName())) // Pārveido katru lomu par SimpleGrantedAuthority
                .collect(Collectors.toSet()); // Saglabā visas lomas kā Set

        // Izveido un atgriež UserDetails objektu, kas satur visu nepieciešamo informāciju par lietotāju
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername()) // Lietotāja vārds
                .password(user.getPassword()) // Lietotāja parole
                .authorities(authorities) // Lietotāja tiesības (roles)
                .accountExpired(false) // Konts nav beidzies
                .accountLocked(false) // Konts nav bloķēts
                .credentialsExpired(false) // Kredenciāļi nav beigušies
                .disabled(!user.isEnabled()) // Konts tiek atspējots, ja lietotājs nav aktīvs
                .build(); // Izveido UserDetails objektu un atgriež
    }
}

