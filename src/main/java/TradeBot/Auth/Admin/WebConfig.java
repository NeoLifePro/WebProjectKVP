package TradeBot.Auth.Admin;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Reģistrējam intercepciju, kas pārbauda katru pieprasījumu, kas sākas ar /admin
        registry.addInterceptor(new HandlerInterceptor() {

            @Override
            public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) throws Exception {
                // Pārbaudām, vai pieprasījuma URI sākas ar "/admin"
                if (req.getRequestURI().startsWith("/admin")) {
                    // Iegūstam pieprasījuma hosta vārdu un pārvēršam to mazajiem burtiem
                    String host = req.getServerName().toLowerCase();

                    // Ja hosta nosaukums neatbilst atļautajiem (admin.ip, localhost, 127.0.0.1, 10.34.31.13),
                    if (!(host.equals("admin.ip") || host.equals("localhost") || host.equals("127.0.0.1") || host.equals("10.34.31.13"))) {
                        // Nosūtām kļūdas atbildi un pārtraucam tālāku pieprasījuma apstrādi
                        res.sendError(HttpServletResponse.SC_NOT_FOUND);
                        return false;
                    }
                }
                return true;
            }
        }).addPathPatterns("/admin/**"); // Pievienojam šo intercepciju tikai ceļiem, kas sākas ar "/admin"
    }
}
