package com.ifortex.internship.emergencyservice.config;

import com.ifortex.internship.medstarter.security.filter.AuthEntryPointJwt;
import com.ifortex.internship.medstarter.security.filter.AuthTokenFilter;
import com.ifortex.internship.medstarter.security.filter.CustomAccessDeniedHandler;
import com.ifortex.internship.medstarter.security.service.JwtTokenValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    public static final String ADMIN = "ADMIN";
    private final JwtTokenValidator jwtTokenValidator;
    private final AuthEntryPointJwt unauthorizedHandler;
    private final CustomAccessDeniedHandler accessDeniedHandler;

    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter() {
        return new AuthTokenFilter(jwtTokenValidator);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(
                session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(
                auth ->
                    auth.requestMatchers(HttpMethod.GET, "/api/v1/allergy").permitAll()
                        .requestMatchers("/api/v1/allergy/**").hasRole(ADMIN)
                        .requestMatchers(HttpMethod.GET, "/api/v1/disease").permitAll()
                        .requestMatchers("/api/v1/disease/**").hasRole(ADMIN)
                        .requestMatchers(HttpMethod.GET, "/api/v1/symptom/**").permitAll()
                        .requestMatchers("/api/v1/symptom/**").hasRole(ADMIN)
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs*/**").permitAll()
                        .anyRequest().authenticated())
            .exceptionHandling(
                exception ->
                    exception
                        .authenticationEntryPoint(unauthorizedHandler)
                        .accessDeniedHandler(accessDeniedHandler));

        http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig)
        throws Exception {
        return authConfig.getAuthenticationManager();
    }
}
