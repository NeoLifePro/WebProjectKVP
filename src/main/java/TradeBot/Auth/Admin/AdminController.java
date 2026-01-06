package TradeBot.Auth.Admin;

import TradeBot.Auth.User;
import TradeBot.Auth.UserRepository;
import TradeBot.Auth.RoleRepository;
import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
@Controller
@RequestMapping("/admin")
public class AdminController {
    private final UserRepository users;
    private final RoleRepository roles;
    private final UserAdminService admin;

    // Konstruktors, kas nodrošina atkarību ievadi
    public AdminController(UserRepository users, RoleRepository roles, UserAdminService admin) {
        this.users = users;
        this.roles = roles;
        this.admin = admin;
    }

    // Lietotāju saraksta lapa
    @GetMapping("/users")
    public String usersPage(@RequestParam(name="q", required=false) String q, // Meklēšanas vaicājums
                            @RequestParam(name="page", defaultValue="0") int page, // Lapas numurs
                            @RequestParam(name="size", defaultValue="10") int size, // Lapu izmērs
                            Model model) {
        // Izveido lapu ar konkrētu sākuma lapu un izmēru
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.max(size, 1), Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<User> result;
        if (StringUtils.hasText(q)) {
            // Ja ir meklēšanas vaicājums
            try {
                // Mēģina pārvērst vaicājuma tekstu uz lietotāja ID
                Long id = Long.parseLong(q.trim());
                result = users.findById(id)
                        .map(u -> new PageImpl<>(List.of(u), pageable, 1)) // Ja lietotājs ar ID ir atrasts
                        .orElseGet(() -> new PageImpl<>(List.of(), pageable, 0)); // Ja nav atrasts, atgriež tukšu lapu
            } catch (NumberFormatException ignored) {
                // Ja meklēšana nav ID, tad meklē pēc lietotāja vārda vai e-pasta
                result = users.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(q, q, pageable);
            }
        } else {
            // Ja nav meklēšanas vaicājuma, atgriež visus lietotājus
            result = users.findAll(pageable);
        }

        model.addAttribute("page", result); // Pievieno lapu modeļa atribūtam
        model.addAttribute("q", q); // Pievieno meklēšanas vaicājumu modeļa atribūtam
        model.addAttribute("allRoles", roles.findAll()); // Pievieno visus lomas no repozitorija
        return "admin/users"; // Atgriež admin lietotāju saraksta skatu
    }

    // API, lai iegūtu lietotāja detaļas JSON formātā
    @GetMapping("/api/users/{id}")
    @ResponseBody
    public ResponseEntity<UserDetailsDto> userDetails(@PathVariable Long id) {
        return admin.details(id) // Iegūst lietotāja detaļas no UserAdminService
                .map(ResponseEntity::ok) // Ja lietotājs ir atrasts, atgriež 200 OK atbildi
                .orElseGet(() -> ResponseEntity.notFound().build()); // Ja lietotājs nav atrasts, atgriež 404 Not Found
    }

    // Lietotāja statusa (bloķēt/atbloķēt) izmaiņas
    @PostMapping("/users/{id}/enabled")
    public String setEnabled(@PathVariable Long id, // Lietotāja ID
                             @RequestParam boolean enabled, // Jauns statuss
                             Authentication auth, // Autentifikācija, lai iegūtu pašreizējo lietotāju
                             RedirectAttributes ra) { // Pāradresēšanas atribūti
        try {
            admin.setEnabled(id, enabled, auth.getName()); // Maina lietotāja statusu
            ra.addFlashAttribute("msg", enabled ? "Atbloķēts" : "Nobloķēts"); // Pievieno ziņojumu pāradresēšanai
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("error", ex.getMessage()); // Pievieno kļūdas ziņojumu
        }
        return "redirect:/admin/users"; // Pāradresē uz lietotāju sarakstu
    }

    // Lietotāja lomu izmaiņas
    @PostMapping("/users/{id}/roles")
    public String setRoles(@PathVariable Long id, // Lietotāja ID
                           @RequestParam(value = "roles", required = false) List<String> roleNames, // Jaunās lomas
                           Authentication auth, // Autentifikācija
                           RedirectAttributes ra) { // Pāradresēšanas atribūti
        try {
            admin.setRoles(id, roleNames, auth.getName()); // Maina lietotāja lomas
            ra.addFlashAttribute("msg", "Atjaunotas piekļuves");
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/users"; // Pāradresē uz lietotāju sarakstu
    }

    // Administratora panelis ar statistiku
    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {
        long total = users.count(); // Kopējais lietotāju skaits
        long active = users.countByEnabled(true); // Aktīvo lietotāju skaits
        long admins = users.countByRoles_Name("ROLE_ADMIN"); // Administrātora lomu skaits

        var latest = users.findAll(
                PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdAt"))
        ).getContent(); // Pēdējo 5 lietotāju saraksts pēc izveides datuma

        model.addAttribute("totalUsers", total); // Pievieno kopējo lietotāju skaitu
        model.addAttribute("activeUsers", active); // Pievieno aktīvo lietotāju skaitu
        model.addAttribute("adminUsers", admins); // Pievieno administrātora lietotāju skaitu
        model.addAttribute("latest", latest); // Pievieno pēdējos lietotājus
        return "admin/dashboard";
    }
}


