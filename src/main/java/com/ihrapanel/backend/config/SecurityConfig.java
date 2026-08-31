package com.ihrapanel.backend.config;

import com.ihrapanel.backend.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter
    ) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

   @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                // JWT kullandığımız için server-side session tutmuyoruz.
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/hello",
                                "/api/auth/register",
                                "/api/companies",
                                "/api/auth/login"
                        ).permitAll()
                  //kendi bilgileirmi getir
                        .requestMatchers(HttpMethod.GET, "/api/users/me")
                         .authenticated()
                   //şirketimde ki bütün kullanıcıları listele
                        .requestMatchers(HttpMethod.GET, "/api/users")
                      .hasRole("OWNER")
                      //owner can read the user by user_id and company id (list the one user)
                      .requestMatchers(HttpMethod.GET, "/api/users/*")
                        .hasRole("OWNER")
                        //owner can create user 
                      .requestMatchers(HttpMethod.POST, "/api/users")
                    .hasRole("OWNER")
               //update the user information only owner can do it
                    .requestMatchers(HttpMethod.PUT, "/api/users/*")
                 .hasRole("OWNER")
             //owner kulllanıcıyı aktif inaktif ediyo
              .requestMatchers(HttpMethod.PATCH, "/api/users/*/active")
                 .hasRole("OWNER")



                        .anyRequest().authenticated()
                )

                // Bizim JWT filtremiz Spring'in username/password
                // filtresinden önce çalışacak.
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

