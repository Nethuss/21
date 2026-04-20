package ru.didcvee.ecd.security.config;

import jakarta.servlet.DispatcherType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import ru.didcvee.ecd.security.filter.AuthTokenFilter;
import ru.didcvee.ecd.security.jwt.AuthEntryPointJwt;
import ru.didcvee.ecd.security.properties.JwtProperties;
import ru.didcvee.ecd.security.service.UserDetailsServiceImpl;

import java.util.List;

/**
 * Конфигурация безопасности веб-приложения. Включает настройку цепочки фильтров безопасности,
 * аутентификации и авторизации.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@EntityScan(basePackages = "ru.unosoft")
public class SecurityConfiguration {

    private final JwtProperties jwtProperties;

    @Autowired
    UserDetailsServiceImpl userDetailsService;

    @Autowired
    private AuthEntryPointJwt unauthorizedHandler;

    @Autowired
    public SecurityConfiguration(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    /**
     * Настройка цепочки фильтров безопасности. Метод настраивает конфигурацию безопасности HTTP для
     * приложения, включая управление сеансами, разрешения на доступ и фильтрацию запросов.
     *
     * @param http объект конфигурации HTTP безопасности, предоставляемый Spring Security,
     *     используемый для настройки параметров безопасности
     * @return настроенный объект SecurityFilterChain, который содержит все примененные настройки и
     *     фильтры безопасности
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // Отключение CSRF
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())

                // Настройка заголовков для возможности загрузки приложения в iframe
                .headers(header -> header.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))

                // Настройка управления сессиями для использования stateless сеансов,
                // что подходит для аутентификации/авторизации с использованием JWT токенов
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Разрешаем доступ без аутентификации и авторизации.
                // Проверку прав доступа выполняем только аннотациями на уровне класса и метода.
                .authorizeHttpRequests(authorize -> authorize
                        .dispatcherTypeMatchers(DispatcherType.ASYNC)
                        .permitAll()
                        .requestMatchers("/api/**")
                        .permitAll()
                        .requestMatchers("/auth/refreshtoken/**")
                        .permitAll()
                        .requestMatchers("/admin/**")
                        .permitAll()
                        .requestMatchers("/auth/signin")
                        .permitAll()
                        .requestMatchers("/auth/signup")
                        .permitAll()
                        .requestMatchers("/auth/signout")
                        .permitAll()
                        .requestMatchers("/tiles/**")
                        .permitAll()
                        .anyRequest()
                        .authenticated())
                .addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class)
                .authenticationProvider(authenticationProvider())
                .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
                // Построение и возврат настроенного объекта SecurityFilterChain
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*")); // разрешаем все origins
        configuration.setAllowedMethods(List.of("*")); // разрешаем все методы
        configuration.setAllowedHeaders(List.of("*")); // разрешаем все заголовки
        configuration.setAllowCredentials(true); // если нужно передавать куки/авторизацию

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();

        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());

        return authProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter() {
        return new AuthTokenFilter();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}
