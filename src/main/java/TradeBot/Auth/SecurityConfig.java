package TradeBot.Auth;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
public class SecurityConfig {
    private final AppUserDetailsService uds;
    public SecurityConfig(AppUserDetailsService uds){ this.uds = uds; }

    @Bean
    PasswordEncoder passwordEncoder(){ return new BCryptPasswordEncoder(); }

    @Bean
    DaoAuthenticationProvider daoAuthProvider(){
        DaoAuthenticationProvider p = new DaoAuthenticationProvider();
        p.setUserDetailsService(uds);
        p.setPasswordEncoder(passwordEncoder());
        return p;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http

                .csrf(csrf -> csrf.ignoringRequestMatchers(
                        new AntPathRequestMatcher("/h2-console/**"),
                        new AntPathRequestMatcher("/api/price/**"),
                        new AntPathRequestMatcher("/api/order/limit/**"),
                        new AntPathRequestMatcher("/api/ind/**"),
                        new AntPathRequestMatcher("/api/indicators/**"),
                        new AntPathRequestMatcher("/api/trades/**"),
                        new AntPathRequestMatcher("/api/market/candles/**"),
                        new AntPathRequestMatcher("/api/balance/**"),
                        new AntPathRequestMatcher("/api/openOrder/**"),
                        new AntPathRequestMatcher("/api/profile/**"),
                        new AntPathRequestMatcher("/admin/users/**")
                ))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/home",
                                "/indicator",
                                "/Login",
                                "/Register",
                                "/css/**",
                                "/img/**",
                                "/js/**",
                                "/h2-console/**",
                                "/api/price/**",
                                "/api/trades/",
                                "/api/order/**",

                                "/api/ind/**",
                                "/api/indicators/**",
                                "/api/market/candles/**",
                                "/api/balance/**",
                                "/api/openOrder/**",
                                "/api/profile/**"
                        ).permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/profile/**").authenticated()
                        .anyRequest().authenticated()

                )
                .formLogin(form -> form
                        .loginPage("/Login")
                        .loginProcessingUrl("/perform_login")
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/home", true)
                        .failureUrl("/Login?error")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/home")
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                .authenticationProvider(daoAuthProvider());

        return http.build();
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
