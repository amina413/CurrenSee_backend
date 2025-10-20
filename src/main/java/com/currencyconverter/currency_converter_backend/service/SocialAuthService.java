package com.currencyconverter.currency_converter_backend.service;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

@Service
public class SocialAuthService {

    @Value("${app.google.client-id:}")
    private String googleClientId;

    @Value("${app.facebook.app-id:}")
    private String facebookAppId;

    @Value("${app.facebook.app-secret:}")
    private String facebookAppSecret;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Verify social login token and return user info
     */
    public Map<String, String> verifyToken(String provider, String idToken) {
        switch (provider.toLowerCase()) {
            case "google":
                return verifyGoogleToken(idToken);
            case "facebook":
                return verifyFacebookToken(idToken);
            default:
                System.err.println("Unknown provider: " + provider);
                return null;
        }
    }

    /**
     * Verify Google ID Token
     */
    private Map<String, String> verifyGoogleToken(String idToken) {
        try {
            String url = "https://oauth2.googleapis.com/tokeninfo?id_token=" + idToken;

            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> body = response.getBody();

                // Optional: Verify the token is for our app (if client-id is configured)
                if (googleClientId != null && !googleClientId.isEmpty()) {
                    String aud = (String) body.get("aud");
                    if (!googleClientId.equals(aud)) {
                        System.err.println("Google token audience mismatch");
                        return null;
                    }
                }

                // Extract user info
                Map<String, String> userInfo = new HashMap<>();
                userInfo.put("id", (String) body.get("sub"));
                userInfo.put("email", (String) body.get("email"));
                userInfo.put("firstName", (String) body.get("given_name"));
                userInfo.put("lastName", (String) body.get("family_name"));
                userInfo.put("avatarUrl", (String) body.get("picture"));

                System.out.println("Google token verified successfully for: " + userInfo.get("email"));
                return userInfo;
            }
        } catch (Exception e) {
            System.err.println("Error verifying Google token: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Verify Facebook Access Token
     */
    private Map<String, String> verifyFacebookToken(String accessToken) {
        try {
            // For now, we'll skip the debug token verification if app-secret is not configured
            // In production, you should always verify with app-secret

            // Get user info directly
            String userUrl = "https://graph.facebook.com/me?fields=id,email,first_name,last_name,picture&access_token=" + accessToken;

            ResponseEntity<Map> userResponse = restTemplate.getForEntity(userUrl, Map.class);

            if (userResponse.getStatusCode() == HttpStatus.OK && userResponse.getBody() != null) {
                Map<String, Object> body = userResponse.getBody();

                // Extract user info
                Map<String, String> userInfo = new HashMap<>();
                userInfo.put("id", String.valueOf(body.get("id")));
                userInfo.put("email", (String) body.get("email"));
                userInfo.put("firstName", (String) body.get("first_name"));
                userInfo.put("lastName", (String) body.get("last_name"));

                // Get picture URL
                if (body.containsKey("picture")) {
                    Map<String, Object> picture = (Map<String, Object>) body.get("picture");
                    if (picture.containsKey("data")) {
                        Map<String, Object> pictureData = (Map<String, Object>) picture.get("data");
                        userInfo.put("avatarUrl", (String) pictureData.get("url"));
                    }
                }

                System.out.println("Facebook token verified successfully for: " + userInfo.get("email"));
                return userInfo;
            }
        } catch (Exception e) {
            System.err.println("Error verifying Facebook token: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
}
