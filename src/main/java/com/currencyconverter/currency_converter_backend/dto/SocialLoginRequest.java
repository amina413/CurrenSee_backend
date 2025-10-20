package com.currencyconverter.currency_converter_backend.dto;


import jakarta.validation.constraints.NotBlank;

public class SocialLoginRequest {
    @NotBlank(message = "Provider is required")
    private String provider; // google, facebook, apple

    @NotBlank(message = "ID token is required")
    private String idToken;

    public SocialLoginRequest() {}

    public SocialLoginRequest(String provider, String idToken) {
        this.provider = provider;
        this.idToken = idToken;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getIdToken() {
        return idToken;
    }

    public void setIdToken(String idToken) {
        this.idToken = idToken;
    }
}
