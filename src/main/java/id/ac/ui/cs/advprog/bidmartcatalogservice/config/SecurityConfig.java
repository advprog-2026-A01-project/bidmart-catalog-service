package id.ac.ui.cs.advprog.bidmartcatalogservice.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, GatewayAuthFilter gatewayAuthFilter) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // health check boleh diakses siapa saja
                        .requestMatchers("/actuator/health").permitAll()
                        // semua endpoint lain harus lewat gateway (dicek oleh GatewayAuthFilter)
                        .anyRequest().permitAll()
                )
                .addFilterBefore(gatewayAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // filter yang mengecek X-Gateway-Secret di setiap request
    @Component
    @RequiredArgsConstructor
    public static class GatewayAuthFilter extends OncePerRequestFilter {

        @Value("${app.gateway.secret}")
        private String gatewaySecret;

        @Override
        protected void doFilterInternal(HttpServletRequest request,
                                        HttpServletResponse response,
                                        FilterChain filterChain) throws ServletException, IOException {

            String path = request.getRequestURI();

            // bypass health check
            if (path.startsWith("/actuator")) {
                filterChain.doFilter(request, response);
                return;
            }

            String incomingSecret = request.getHeader("X-Gateway-Secret");

            if (!gatewaySecret.equals(incomingSecret)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"Forbidden\", \"message\": \"Missing or invalid gateway secret\"}");
                return;
            }

            filterChain.doFilter(request, response);
        }
    }
}
