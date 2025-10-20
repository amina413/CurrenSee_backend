package com.currencyconverter.currency_converter_backend.controller;

import com.currencyconverter.currency_converter_backend.config.JwtUtils;
import com.currencyconverter.currency_converter_backend.dto.JwtResponse;
import com.currencyconverter.currency_converter_backend.dto.LoginRequest;
import com.currencyconverter.currency_converter_backend.dto.MessageResponse;
import com.currencyconverter.currency_converter_backend.dto.SignupRequest;
import com.currencyconverter.currency_converter_backend.dto.SocialLoginRequest;
import com.currencyconverter.currency_converter_backend.entity.User;
import com.currencyconverter.currency_converter_backend.repository.UserRepository;
import com.currencyconverter.currency_converter_backend.service.UserDetailsImpl;
import com.currencyconverter.currency_converter_backend.service.UserDetailsServiceImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    // IMPORTANT: Set this to your Google Web Client ID
    private static final String GOOGLE_WEB_CLIENT_ID = "79518038843-jp6o3gvehliq7u7j22j6qk3f8sqgk52n.apps.googleusercontent.com";

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    UserDetailsServiceImpl userDetailsService;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        return ResponseEntity.ok(new JwtResponse(
                jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                getUserFirstName(userDetails.getId()),
                getUserLastName(userDetails.getId())
        ));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Username is already taken!"));
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Email is already in use!"));
        }

        User user = new User(
                signUpRequest.getUsername(),
                signUpRequest.getEmail(),
                encoder.encode(signUpRequest.getPassword()),
                signUpRequest.getFirstName(),
                signUpRequest.getLastName()
        );

        userRepository.save(user);
        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }

    // NEW: Social login (Google)
    @PostMapping("/social")
    public ResponseEntity<?> socialLogin(@Valid @RequestBody SocialLoginRequest request) {
        String provider = request.getProvider();
        String idToken = request.getIdToken();

        if (provider == null || idToken == null) {
            return ResponseEntity.badRequest().body(new MessageResponse("Invalid social login request"));
        }

        if (!provider.equalsIgnoreCase("google")) {
            // You can extend here for 'facebook' later if needed
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                    .body(new MessageResponse("Provider not supported: " + provider));
        }

        try {
            // Verify Google ID token using tokeninfo endpoint
            // NOTE: For production, prefer Google's Java library verification. This is a minimal approach.
            URI uri = URI.create("https://oauth2.googleapis.com/tokeninfo?id_token=" + idToken);
            HttpRequest httpRequest = HttpRequest.newBuilder().uri(uri).GET().build();
            HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (httpResponse.statusCode() != 200) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new MessageResponse("Invalid Google ID token"));
            }

            JsonNode tokenInfo = objectMapper.readTree(httpResponse.body());
            String audience = tokenInfo.path("aud").asText(null);
            String email = tokenInfo.path("email").asText(null);
            String name = tokenInfo.path("name").asText(null);
            String givenName = tokenInfo.path("given_name").asText(null);
            String familyName = tokenInfo.path("family_name").asText(null);
            boolean emailVerified = tokenInfo.path("email_verified").asText("false").equalsIgnoreCase("true");

            // Validate audience (must match your Web Client ID)
            if (audience == null || !audience.equals(GOOGLE_WEB_CLIENT_ID)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new MessageResponse("Google token audience mismatch"));
            }

            if (email == null || !emailVerified) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new MessageResponse("Google email not available or not verified"));
            }

            // Upsert user by email
            User user = userRepository.findByEmail(email).orElseGet(() -> {
                String usernameBase = email.split("@")[0];
                String username = usernameBase;

                // Ensure unique username if needed
                int suffix = 1;
                while (userRepository.existsByUsername(username)) {
                    username = usernameBase + suffix;
                    suffix++;
                }

                User u = new User(
                        username,
                        email,
                        // Social accounts won't use password login; store a random/encoded placeholder
                        encoder.encode(java.util.UUID.randomUUID().toString()),
                        givenName != null && !givenName.isBlank() ? givenName : (name != null ? name.split(" ")[0] : ""),
                        familyName != null ? familyName : (name != null && name.contains(" ") ? name.substring(name.indexOf(' ') + 1) : "")
                );
                return userRepository.save(u);
            });

            // Authenticate the user in Spring Security context
            // Load UserDetails from your service
            var userDetails = userDetailsService.loadUserByUsername(user.getUsername());
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities()
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Issue JWT
            String jwt = jwtUtils.generateJwtToken(authentication);

            return ResponseEntity.ok(new JwtResponse(
                    jwt,
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    getUserFirstName(user.getId()),
                    getUserLastName(user.getId())
            ));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Google social login failed"));
        }
    }

    @GetMapping("/test")
    public ResponseEntity<?> testEndpoint() {
        return ResponseEntity.ok(new MessageResponse("Auth controller is working!"));
    }

    // Helper method to get user's first name
    private String getUserFirstName(Long userId) {
        return userRepository.findById(userId)
                .map(User::getFirstName)
                .orElse("");
    }

    // Helper method to get user's last name
    private String getUserLastName(Long userId) {
        return userRepository.findById(userId)
                .map(User::getLastName)
                .orElse("");
    }
}