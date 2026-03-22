package org.tutorbooking.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
<<<<<<< HEAD
=======
import org.springframework.security.authentication.dao.DaoAuthenticationProvider; 
>>>>>>> UPDATE
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
<<<<<<< HEAD
=======
import org.springframework.beans.factory.annotation.Autowired; 
>>>>>>> UPDATE

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
<<<<<<< HEAD
=======

    @Autowired
    private CustomUserDetailsService userDetailsService;

>>>>>>> UPDATE
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter();
    }

<<<<<<< HEAD
=======
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
>>>>>>> UPDATE

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
<<<<<<< HEAD
                .authorizeHttpRequests(auth -> auth
                        // 1. Mở cửa cho trang chủ, login và các file tĩnh
                        .requestMatchers("/", "/login/**", "/oauth2/**").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                        // 2. Phân quyền theo vai trò
                        .requestMatchers("/api/parent/**").hasRole("PARENT")
                        .requestMatchers("/api/tutor/**").hasRole("TUTOR")

                        // 3. Còn lại phải đăng nhập
                        .anyRequest().authenticated())
                // 4. Kích hoạt đăng nhập bằng Google (OAuth2)
                .oauth2Login(oauth2 -> oauth2
                        .defaultSuccessUrl("/api/auth/google-success", true) // Đường dẫn sau khi login thành công
=======
                
                .authenticationProvider(authenticationProvider()) 
                
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/logout", "/api/auth/change-password").authenticated()
                        .requestMatchers("/", "/login/**", "/oauth2/**").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/api/parent/**").hasRole("PARENT")
                        .requestMatchers("/api/tutor/**").hasRole("TUTOR")
                        .anyRequest().authenticated())
                .oauth2Login(oauth2 -> oauth2
                        .defaultSuccessUrl("/api/auth/google-success", true) 
>>>>>>> UPDATE
                );

        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000",
<<<<<<< HEAD
                "https://tutors-booking-management-fe.vercel.app"
=======
                "https://tutors-booking-management-fe.vercel.app",
                "http://localhost:8080" 
>>>>>>> UPDATE
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*")) ;
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
