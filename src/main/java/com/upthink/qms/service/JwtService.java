package com.upthink.qms.service;


import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.SignedJWT;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminAddUserToGroupRequest;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class JwtService {

    @Value("${spring.security.oauth2.client.provider.cognito.jwk-set-uri}")
    private String JWKS_URL;
    private Map<String, RSAKey> registrationKeysMap = new ConcurrentHashMap<>();

    public JwtService() {
//        this.JWKS_URL = jwksUrl;
//        // Load initial keys from the Cognito JWK Set URI
//        if (JWKS_URL == null || JWKS_URL.isEmpty()) {
//            throw new RuntimeException("JWKS_URL is not set or is empty");
//        }
//        loadKeysFromCognito();
    }

    @PostConstruct
    public void init() {
        // Validate JWKS_URL
        if (JWKS_URL == null || JWKS_URL.isEmpty()) {
            throw new RuntimeException("JWKS_URL is not set or is empty");
        }
        // Load keys from Cognito
        loadKeysFromCognito();
    }


    /**
     * Loads keys from the Cognito JWK Set URI and populates the registrationKeysMap.
     */
    private void loadKeysFromCognito() {
        try {
            // Fetch JWK set from Cognito URI
            InputStream inputStream = URI.create(JWKS_URL).toURL().openStream();
//            InputStream inputStream = new URL(JWKS_URL).openStream();
            JWKSet jwkSet = JWKSet.load(inputStream);

            // Iterate over each key in the JWK set and store it in the map
            for (JWK jwk : jwkSet.getKeys()) {
                if (jwk instanceof RSAKey) {
                    RSAKey rsaKey = (RSAKey) jwk;
                    registrationKeysMap.put(rsaKey.getKeyID(), rsaKey);
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to load JWKs from Cognito URI", e);
        }
    }

    /**
     * Retrieves the RSAKey for the given key ID (kid).
     *
     * @param kid The key ID.
     * @return The RSAKey associated with the key ID.
     */
    public synchronized RSAKey getJsonWebKey(String kid) {
        if (!registrationKeysMap.containsKey(kid)) {
            throw new RuntimeException("Key ID not found: " + kid);
        }
        return registrationKeysMap.get(kid);
    }

    /**
     * Returns the list of public keys in the JWK format.
     *
     * @return A list of public keys in JWK format.
     */
    public List<Map<String, Object>> getKeyList() {
        List<Map<String, Object>> keyList = new ArrayList<>();
        for (RSAKey rsaKey : registrationKeysMap.values()) {
            try {
                RSAKey publicKey = rsaKey.toPublicJWK();
                Map<String, Object> keyData = new HashMap<>();
                keyData.put("kty", publicKey.getKeyType().getValue());
                keyData.put("alg", "RS256");
                keyData.put("kid", publicKey.getKeyID());
                keyData.put("e", publicKey.getPublicExponent().toString());
                keyData.put("n", publicKey.getModulus().toString());
                keyData.put("use", "sig");
                keyList.add(keyData);
            } catch (Exception e) {
                throw new RuntimeException("Failed to convert RSA key to public key", e);
            }
        }
        return keyList;
    }

    /**
     * Returns the full JWK Set (JSON Web Key Set).
     *
     * @return The JWK Set.
     */
    public JWKSet getJwkSet() {
        return new JWKSet(new ArrayList<>(registrationKeysMap.values()));
    }

    /**
     * Extracts the "sub" claim (Cognito User ID) from the given JWT token.
     *
     * @param token The JWT token.
     * @return The "sub" claim value (Cognito User ID).
     */
    public String extractCognitoSub(String token) {
        try {
            // Parse the JWT token
            SignedJWT signedJWT = SignedJWT.parse(token);

            // Get the claims set from the JWT
            String cognitoSub = signedJWT.getJWTClaimsSet().getStringClaim("sub");

            // Return the "sub" claim value
            return cognitoSub;
        } catch (ParseException e) {
            throw new RuntimeException("Failed to parse JWT and extract 'sub' claim", e);
        }
    }

    /**
     * Extracts the "cognito:groups" claim (Cognito User Groups) from the given JWT token.
     *
     * @param token The JWT token.
     * @return A list of groups the user belongs to.
     */
    public List<String> extractCognitoGroups(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            // Get the list of groups the user belongs to
            return signedJWT.getJWTClaimsSet().getStringListClaim("cognito:groups");
        } catch (ParseException e) {
            throw new RuntimeException("Failed to parse JWT and extract 'groups' claim", e);
        }
    }

    /**
     * Validates the JWT token. It verifies the token's signature and checks if it's expired.
     *
     * @param token The JWT token.
     * @return True if the token is valid, false otherwise.
     */
    public boolean isTokenValid(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            String kid = signedJWT.getHeader().getKeyID();
            System.out.println("Jwt service kid " + kid);
            RSAKey rsaKey = registrationKeysMap.get(kid);
            if (rsaKey == null) {
                throw new RuntimeException("Key ID not found: " + kid);
            }

            // Validate the token signature
            RSASSAVerifier verifier = new RSASSAVerifier(rsaKey.toRSAPublicKey());
            boolean isSignatureValid = signedJWT.verify(verifier);

            // Check if token is expired
            Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();
            boolean isExpired = expirationTime == null || new Date().after(expirationTime);

            return isSignatureValid && !isExpired;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Extracts the expiration time of the token.
     *
     * @param token The JWT token.
     * @return The expiration date of the token.
     */
    public Date extractExpiration(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            return signedJWT.getJWTClaimsSet().getExpirationTime();
        } catch (ParseException e) {
            throw new RuntimeException("Failed to parse JWT and extract expiration", e);
        }
    }

    /**
     * Extracts the "email" claim (Cognito User Email) from the given JWT token.
     *
     * @param token The JWT token.
     * @return The "email" claim value (Cognito User Email).
     */
    public String extractCognitoEmail(String token) {
        try {
            // Parse the JWT token
            SignedJWT signedJWT = SignedJWT.parse(token);
            // Get the "email" claim from the JWT
            String email = signedJWT.getJWTClaimsSet().getStringClaim("email");
            // Return the email claim value
            return email;
        } catch (ParseException e) {
            throw new RuntimeException("Failed to parse JWT and extract 'email' claim", e);
        }
    }

//    public void addUserToCognitoGroup(String email, String groupName) {
//        try {
//            AdminAddUserToGroupRequest request = AdminAddUserToGroupRequest.builder()
//                    .userPoolId(userPoolId)
//                    .username(email)
//                    .groupName(groupName)
//                    .build();
//
//            AdminAddUserToGroupResponse response = cognitoClient.adminAddUserToGroup(request);
//            // Handle response if needed
//        } catch (CognitoIdentityProviderException e) {
//            throw new RuntimeException("Failed to add user to Cognito group: " + e.awsErrorDetails().errorMessage(), e);
//        }
//    }

}