//package boostech.code.security.config;
//
//import boostech.code.security.jwt.AuthEntryPointJwt;
//import boostech.code.security.jwt.AuthTokenFilter;
//import boostech.code.service.serviceImpl.UserDetailServiceImpl;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
//import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
//import org.springframework.security.config.http.SessionCreationPolicy;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.security.web.SecurityFilterChain;
//
//@Configuration
//@EnableWebSecurity
//public class WebSecurityConfig {
//
//    UserDetailServiceImpl userDetailsService;
//    AuthEntryPointJwt unauthorizedHandler;
//
//    @Autowired
//    public WebSecurityConfig(UserDetailServiceImpl userDetailsService, AuthEntryPointJwt unauthorizedHandler) {
//        this.userDetailsService = userDetailsService;
//        this.unauthorizedHandler = unauthorizedHandler;
//    }
//
//    @Bean
//    public AuthTokenFilter authenticationJwtTokenFilter() {
//        return new AuthTokenFilter();
//    }
//
//    @Bean
//    public DaoAuthenticationProvider authenticationProvider() {
//        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
//
//        authenticationProvider.setUserDetailsService(userDetailsService);
//        authenticationProvider.setPasswordEncoder(passwordEncoder());
//
//        return authenticationProvider;
//    }
//
//    @Bean
//    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
//        return authConfig.getAuthenticationManager();
//    }
//
//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder();
//    }
//
//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//       http.csrf(AbstractHttpConfigurer::disable)
//               .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
//               .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//               .authorizeHttpRequests(auth ->
//                   auth.requestMatchers("/api/v1/auth/**").permitAll()
//                           .requestMatchers("/api/test/**").permitAll()
//                           .requestMatchers("/api/v1/**").permitAll()
//                           .anyRequest().authenticated()
//               );
//
//
//       http.authenticationProvider(authenticationProvider());
//       http.addFilterBefore(authenticationJwtTokenFilter(), AuthTokenFilter.class);
//
//         return http.build();
//    }
//}
