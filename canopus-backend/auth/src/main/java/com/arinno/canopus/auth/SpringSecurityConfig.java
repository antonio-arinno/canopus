package com.arinno.canopus.auth;

import java.util.Arrays;
import java.util.Objects;

import javax.crypto.SecretKey;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import com.arinno.canopus.auth.filters.JwtAuthenticationFilter;
import com.arinno.canopus.auth.filters.JwtValidationFilter;

@Configuration
public class SpringSecurityConfig {

    private final AuthenticationConfiguration authenticationConfiguration;
    private final SecretKey jwtSecretKey;

    SpringSecurityConfig(AuthenticationConfiguration authenticationConfiguration,
            @Value("${app.jwt.secret}") String encodedJwtSecret) {
        this.authenticationConfiguration = authenticationConfiguration;
        this.jwtSecretKey = TokenJwtConfig.secretKey(encodedJwtSecret);
    }

    @Bean
    AuthenticationManager authenticationManager() throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http.authorizeHttpRequests(authz -> authz
            .requestMatchers(HttpMethod.POST, "/register").permitAll()
            .requestMatchers(HttpMethod.GET, "/actuator/health").permitAll()
            .requestMatchers("/actuator/**").hasRole("ADMIN")
            .requestMatchers(HttpMethod.GET, "/user/**").authenticated()
            .requestMatchers(HttpMethod.PUT, "/user/me").authenticated()
            .requestMatchers(HttpMethod.PUT, "/user/me/password").authenticated()
            .requestMatchers(HttpMethod.POST, "/user").hasRole("ADMIN")
            .requestMatchers(HttpMethod.DELETE, "/user/*").hasRole("ADMIN")
            .anyRequest().authenticated())
            .cors(cors -> cors.configurationSource(configurationSource()))
            .addFilter(new JwtAuthenticationFilter(authenticationManager(), jwtSecretKey))
            .addFilter(new JwtValidationFilter(authenticationManager(), jwtSecretKey))                
            .csrf(config -> config.disable())
            .sessionManagement(management -> management.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .build();
    }

    @Bean
    CorsConfigurationSource configurationSource(){
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.asList("http://13.36.165.162", "http://localhost:4200"));
        config.setAllowedMethods(Arrays.asList("POST","GET","PUT","DELETE"));
        config.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
        config.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    FilterRegistrationBean<CorsFilter> corsFilter(){
        CorsConfigurationSource corsConfigurationSource = Objects.requireNonNull(
            this.configurationSource(), "cors configuration source must not be null");
        FilterRegistrationBean<CorsFilter> corsBean = new FilterRegistrationBean<>(
            new CorsFilter(corsConfigurationSource));
        corsBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return corsBean;
    }

}
