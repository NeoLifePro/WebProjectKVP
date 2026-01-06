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
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) throws Exception {
                if (req.getRequestURI().startsWith("/admin")) {
                    String host = req.getServerName().toLowerCase();
                    if (!(host.equals("admin.ip") || host.equals("localhost") || host.equals("127.0.0.1") || host.equals("10.34.31.13"))) {
                        res.sendError(HttpServletResponse.SC_NOT_FOUND);
                        return false;
                    }
                }
                return true;
            }
        }).addPathPatterns("/admin/**");
    }
}
