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

    public AdminController(UserRepository users, RoleRepository roles, UserAdminService admin) {
        this.users = users; this.roles = roles; this.admin = admin;
    }

    @GetMapping("/users")
    public String usersPage(@RequestParam(name="q", required=false) String q,
                            @RequestParam(name="page", defaultValue="0") int page,
                            @RequestParam(name="size", defaultValue="10") int size,
                            Model model) {
        Pageable pageable = PageRequest.of(Math.max(page,0), Math.max(size,1), Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<User> result;
        if (StringUtils.hasText(q)) {

            try {
                Long id = Long.parseLong(q.trim());
                result = users.findById(id)
                        .map(u -> new PageImpl<>(List.of(u), pageable, 1))
                        .orElseGet(() -> new PageImpl<>(List.of(), pageable, 0));
            } catch (NumberFormatException ignored) {
                result = users.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(q, q, pageable);
            }
        } else {
            result = users.findAll(pageable);
        }

        model.addAttribute("page", result);
        model.addAttribute("q", q);
        model.addAttribute("allRoles", roles.findAll());
        return "admin/users";
    }

    // JSON для модалки
    @GetMapping("/api/users/{id}")
    @ResponseBody
    public ResponseEntity<UserDetailsDto> userDetails(@PathVariable Long id) {
        return admin.details(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // блок/разблок
    @PostMapping("/users/{id}/enabled")
    public String setEnabled(@PathVariable Long id,
                             @RequestParam boolean enabled,
                             Authentication auth,
                             RedirectAttributes ra) {
        try {
            admin.setEnabled(id, enabled, auth.getName());
            ra.addFlashAttribute("msg", enabled ? "Atbloķēts" : "Nobloķēts");
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/users";
    }

    // смена ролей
    @PostMapping("/users/{id}/roles")
    public String setRoles(@PathVariable Long id,
                           @RequestParam(value = "roles", required = false) List<String> roleNames,
                           Authentication auth,
                           RedirectAttributes ra) {
        try {
            admin.setRoles(id, roleNames, auth.getName());
            ra.addFlashAttribute("msg", "Atjaunotas piekļuves");
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/users";
    }


    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {
        long total = users.count();
        long active = users.countByEnabled(true);
        long admins = users.countByRoles_Name("ROLE_ADMIN");

        var latest = users.findAll(
                PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdAt"))
        ).getContent();

        model.addAttribute("totalUsers", total);
        model.addAttribute("activeUsers", active);
        model.addAttribute("adminUsers", admins);
        model.addAttribute("latest", latest);
        return "admin/dashboard"; // templates/admin/dashboard.html
    }

}

