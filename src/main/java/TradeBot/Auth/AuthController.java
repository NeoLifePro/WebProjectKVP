package TradeBot.Auth;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
public class AuthController {
    private final UserService userService;
    public AuthController(UserService userService){ this.userService = userService; }


    @GetMapping("/Login")
    public String loginPage(){ return "/auth/login"; } // templates/login.html


    @GetMapping("/Register")
    public String registerPage(Model model){
        model.addAttribute("form", new RegisterDto());
        return "/auth/register"; // templates/register.html
    }


    @PostMapping("/Register")
    public String doRegister(@ModelAttribute("form") RegisterDto form,
                             RedirectAttributes ra){
        try {
            userService.register(form);
            ra.addFlashAttribute("msg", "Account created. You can log in now.");
            return "redirect:/Login";
        } catch (IllegalArgumentException ex){
            ra.addFlashAttribute("error", ex.getMessage());
            return "redirect:/Register";
        }
    }
}