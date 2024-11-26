package boostech.code.controllers;

import boostech.code.models.ERole;
import boostech.code.models.Role;
import boostech.code.models.User;
import boostech.code.payload.request.LoginRequest;
import boostech.code.payload.request.SignupRequest;
import boostech.code.payload.response.JwtResponse;
import boostech.code.payload.response.MessageResponse;
import boostech.code.repository.RoleRepository;
import boostech.code.repository.UserRepository;
import boostech.code.service.serviceImpl.UserDetailsImpl;
import boostech.code.utils.JwtUtils;
import jakarta.validation.Valid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.security.core.Authentication;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
        @Autowired
        AuthenticationManager authenticationManager;
        @Autowired
        private  UserRepository userRepository;
        @Autowired
        private  RoleRepository roleRepository;
        @Autowired
        private  PasswordEncoder passwordEncoder;
        @Autowired
        private  JwtUtils jwtUtils;
        private final Logger logger = LogManager.getLogger(AuthController.class);

        @PostMapping("/signup")
        public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
                try {
                        logger.info("Starting registration for username: {}", signUpRequest.getUsername());

                        // Check username
                        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
                                logger.warn("Username {} is already taken", signUpRequest.getUsername());
                                return ResponseEntity
                                        .badRequest()
                                        .body(new MessageResponse("Error: Username is already taken!"));
                        }

                        // Check email
                        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
                                logger.warn("Email {} is already in use", signUpRequest.getEmail());
                                return ResponseEntity
                                        .badRequest()
                                        .body(new MessageResponse("Error: Email is already in use!"));
                        }

                        // Create new user
                        User user = new User(signUpRequest.getUsername(), signUpRequest.getEmail(), signUpRequest.getPassword());

                        Set<String> strRoles = signUpRequest.getRole();
                        Set<Role> roles = new HashSet<>();

                        if (strRoles == null || strRoles.isEmpty()) {
                                Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                                        .orElseThrow(() -> new RuntimeException("Error: Default role (ROLE_USER) not found."));
                                roles.add(userRole);
                                logger.info("Assigning default ROLE_USER to {}", user);
                        } else {
                                strRoles.forEach(role -> {
                                        logger.info("Processing role: {}", role);
                                        Role newRole = switch (role.toLowerCase()) {
                                            case "admin" -> roleRepository.findByName(ERole.ROLE_ADMIN)
                                                    .orElseThrow(() -> new RuntimeException("Error: ROLE_ADMIN not found."));
                                            case "mod" -> roleRepository.findByName(ERole.ROLE_MODERATOR)
                                                    .orElseThrow(() -> new RuntimeException("Error: ROLE_MODERATOR not found."));
                                            default -> roleRepository.findByName(ERole.ROLE_USER)
                                                    .orElseThrow(() -> new RuntimeException("Error: ROLE_USER not found."));
                                        };
                                    roles.add(newRole);
                                });
                        }

                        user.setRoles(roles);
                        user.setPassword(passwordEncoder.encode(user.getPassword()));
                        userRepository.save(user);

                        logger.info("User {} registered successfully with roles: {}",
                                user.getUsername(),
                                roles.stream().map(r -> r.getName().toString()).collect(Collectors.joining(", ")));

                        return ResponseEntity.ok(Map.of(
                                "message", "User registered successfully.",
                                "username", user.getUsername(),
                                "email", user.getEmail(),
                                "roles", roles.stream()
                                        .map(role -> role.getName().toString())
                                        .collect(Collectors.toSet())
                        ));

                } catch (Exception e) {
                        logger.error("Error during user registration: ", e);
                        return ResponseEntity
                                .internalServerError()
                                .body(new MessageResponse("Registration failed: " + e.getMessage()));
                }
        }
//        @PostMapping("/signin")
//        public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
//                Authentication authentication = authenticationManager.authenticate(
//                        new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
//                );
//
//                SecurityContextHolder.getContext().setAuthentication(authentication);
//                String jwt = jwtUtils.generateJwtToken(authentication);
//
//                UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
//                List<String> roles = userDetails.getAuthorities().stream()
//                        .map(GrantedAuthority::getAuthority)
//                        .collect(Collectors.toList());
//
//                return ResponseEntity.ok(new JwtResponse(jwt ,
//                        userDetails.getId(),
//                        userDetails.getEmail(),
//                        userDetails.getUsername(),
//                        roles));
//        }
        @PostMapping("/signin")
        public ResponseEntity<JwtResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
                Authentication authentication = authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
                );

                UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

                String jwt = jwtUtils.generateJwtToken(authentication);

                long expirationTime = jwtUtils.getExpirationTime();

                JwtResponse jwtResponse = new JwtResponse(
                        jwt,
                        userDetails.getId(),
                        userDetails.getUsername(),
                        userDetails.getEmail(),
                        userDetails.getAuthorities().stream()
                                .map(GrantedAuthority::getAuthority)
                                .collect(Collectors.toList()),
                        expirationTime
                );


                return ResponseEntity.ok(jwtResponse);
        }



}
