package com.upthink.qms.service;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CustomGrantedAuthoritiesMapper implements GrantedAuthoritiesMapper {

    @Override
    public Collection<? extends GrantedAuthority> mapAuthorities(Collection<? extends GrantedAuthority> authorities) {
        System.out.println("Authorities " + authorities);

        // Check if the authentication is OAuth2AuthenticationToken
        if (authorities.stream().anyMatch(auth -> auth instanceof OAuth2AuthenticationToken)) {
            OAuth2AuthenticationToken oAuth2Token = (OAuth2AuthenticationToken) authorities.iterator().next();
            OAuth2User user = oAuth2Token.getPrincipal();

            // If it's an OIDC user, retrieve cognito:groups from claims
            if (user instanceof OidcUser) {
                OidcUser oidcUser = (OidcUser) user;
                List<String> groups = oidcUser.getClaimAsStringList("cognito:groups");
                System.out.println("GROUPS: " + groups);
                if (groups != null) {
                    // Map groups to Spring Security authorities with ROLE_ prefix
                    return groups.stream()
                            .map(group -> new SimpleGrantedAuthority("ROLE_" + group))
                            .collect(Collectors.toList());
                }
            }
        }

        // Default fallback if no groups are found
        return authorities;
    }
}
