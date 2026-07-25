package SimulaI.config;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import SimulaI.exception.ErroResponseDTO;
import SimulaI.service.TokenService;

/**
 * API stateless (JWT via header Authorization): sem sessão, sem CSRF (não há cookie de
 * sessão para explorar), sem login/basic-auth padrão do Spring (que redirecionaria para
 * uma tela de login em vez de responder 401/403 em JSON, quebrando o contrato de uma API REST).
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final UserDetailsService usuarioDetailsService;
    private final TokenService tokenService;
    private final ObjectMapper objectMapper;

    @Value("${simulai.frontend-url}")
    private String frontendUrl;

    public SecurityConfig(UserDetailsService usuarioDetailsService, TokenService tokenService, ObjectMapper objectMapper) {
        this.usuarioDetailsService = usuarioDetailsService;
        this.tokenService = tokenService;
        this.objectMapper = objectMapper;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(usuarioDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(tokenService, usuarioDetailsService);
    }

    @Bean
    public AuthRateLimitFilter authRateLimitFilter() {
        return new AuthRateLimitFilter(objectMapper);
    }

    /**
     * Frontend (Vercel) e backend (Render) ficam em domínios diferentes em produção —
     * sem isso o navegador bloqueia toda chamada da SPA. Em dev não faz diferença porque
     * o proxy do Vite já mantém tudo na mesma origem do ponto de vista do navegador.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuracao = new CorsConfiguration();
        configuracao.setAllowedOrigins(List.of(frontendUrl));
        configuracao.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuracao.setAllowedHeaders(List.of("*"));
        configuracao.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuracao);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // públicos: login, cadastro de usuário e documentação
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/usuarios").permitAll()
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/actuator/health").permitAll()

                        // listagem de moderação: expõe o gabarito das alternativas, somente ADMIN
                        .requestMatchers(HttpMethod.GET, "/api/questoes/moderacao").hasRole("ADMIN")

                        // curadoria do catálogo (bancas/disciplinas/assuntos/concursos), geração
                        // de questões via IA e moderação de questões: somente ADMIN
                        .requestMatchers(HttpMethod.POST, "/api/bancas/**", "/api/disciplinas/**", "/api/assuntos/**", "/api/concursos/**", "/api/questoes/**")
                        .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/bancas/**", "/api/disciplinas/**", "/api/assuntos/**", "/api/concursos/**", "/api/questoes/**")
                        .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/bancas/**", "/api/disciplinas/**", "/api/assuntos/**", "/api/concursos/**", "/api/questoes/**")
                        .hasRole("ADMIN")

                        // administração de usuários: listar todos, buscar por email e alterar role
                        .requestMatchers(HttpMethod.GET, "/api/usuarios", "/api/usuarios/email").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/usuarios/**").hasRole("ADMIN")

                        // demais rotas (inclui GETs de catálogo, Usuario/{id} e Simulado,
                        // cuja ownership é verificada via @PreAuthorize no Controller)
                        .anyRequest().authenticated())
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(authRateLimitFilter(), UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(this::responderNaoAutenticado)
                        .accessDeniedHandler(this::responderAcessoNegado));

        return http.build();
    }

    private void responderNaoAutenticado(HttpServletRequest request, HttpServletResponse response,
                                          org.springframework.security.core.AuthenticationException authException) throws IOException {
        escreverErro(request, response, HttpStatus.UNAUTHORIZED, "Autenticação necessária.");
    }

    private void responderAcessoNegado(HttpServletRequest request, HttpServletResponse response,
                                        org.springframework.security.access.AccessDeniedException accessDeniedException) throws IOException {
        escreverErro(request, response, HttpStatus.FORBIDDEN, "Você não tem permissão para executar esta ação.");
    }

    /**
     * Esses handlers rodam na cadeia de filtros, antes do DispatcherServlet — não passam
     * pelo GlobalExceptionHandler, por isso escrevem o mesmo formato de erro manualmente.
     */
    private void escreverErro(HttpServletRequest request, HttpServletResponse response, HttpStatus status,
                               String mensagem) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ErroResponseDTO erro = new ErroResponseDTO(LocalDateTime.now(), status.value(), status.getReasonPhrase(),
                mensagem, request.getRequestURI());
        objectMapper.writeValue(response.getWriter(), erro);
    }
}
