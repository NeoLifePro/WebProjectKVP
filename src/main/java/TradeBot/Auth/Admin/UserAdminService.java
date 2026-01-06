package TradeBot.Auth.Admin;

import TradeBot.Auth.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class UserAdminService {
    private final UserRepository users;
    private final RoleRepository roles;

    // Konstruktoram tiek nodoti lietotāju un lomu repozitoriji
    public UserAdminService(UserRepository users, RoleRepository roles) {
        this.users = users;
        this.roles = roles;
    }

    // Šī metode atgriež informāciju par lietotāju (skatījumā no lietotāja ID)
    @Transactional(readOnly = true) // Lasīšanas operācijas, lai izvairītos no datu modificēšanas
    public Optional<UserDetailsDto> details(Long id){
        return users.findById(id).map(u -> new UserDetailsDto(
                u.getId(), u.getUsername(), u.getEmail(), u.isEnabled(),
                u.getRoles().stream().map(Role::getName).toList(), // Pārveido lomas uz to nosaukumiem
                u.getCreatedAt(), u.getLastLoginAt() // Lietotāja izveidošanas un pēdējā pieslēgšanās laiks
        ));
    }

    // Šī metode ļauj mainīt lietotāja statusu (ieslēgts/izslēgts)
    @Transactional
    public void setEnabled(Long userId, boolean enabled, String acting){
        User u = users.findById(userId).orElseThrow(); // Mēģina atrast lietotāju pēc ID
        // Ja lietotājs mēģina izslēgt pats sevi, tiek izmests izņēmums
        if (u.getUsername().equals(acting))
            throw new IllegalArgumentException("nedrikst bloķēt sevi...");
        u.setEnabled(enabled); // Maina lietotāja statusu
        users.save(u); // Saglabā izmaiņas datubāzē
    }

    // Šī metode ļauj mainīt lietotāja lomas
    @Transactional
    public void setRoles(Long userId, List<String> roleNames, String acting){
        User u = users.findById(userId).orElseThrow(); // Mēģina atrast lietotāju pēc ID
        Set<String> names = new HashSet<>(Optional.ofNullable(roleNames).orElseGet(List::of)); // Pārvērš lomu sarakstu par Set, lai novērstu dublikātus
        // Ja lietotājs mēģina noņemt sev "ROLE_ADMIN", tad tiek izmests izņēmums
        if (u.getUsername().equals(acting) && !names.contains("ROLE_ADMIN"))
            throw new IllegalArgumentException("nedrikts noņemt sev Admin");
        // Ja lomu saraksts ir tukšs, tad tiek izmests izņēmums
        if (names.isEmpty()) throw new IllegalArgumentException("Nepieceišama vismaz viena piekļuve(ROLE)");

        Set<Role> newRoles = new HashSet<>(); // Jaunu lomu iestatīšana
        // Pārveido lomu nosaukumus uz Role objektiem
        for (String n : names) {
            Role r = roles.findByName(n).orElseGet(() -> roles.save(new Role(n))); // Ja loma nav atrasta, tiek izveidota jauna
            newRoles.add(r); // Pievieno lomu
        }
        u.setRoles(newRoles); // Pievieno jaunas lomas lietotājam
        users.save(u); // Saglabā izmaiņas datubāzē
    }
}
