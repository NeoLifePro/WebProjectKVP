package TradeBot.Controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LinkController {
    @GetMapping("/indicator")
    public String GetIndicator(){return "indicator";}
    @GetMapping("/trade")
    public String GetTrade(){return "trade";}
    @GetMapping("/home")
    public String GetHome(){return "home";}
    @GetMapping("/profile")
    public String GetProfile(){return "profile";}

}
