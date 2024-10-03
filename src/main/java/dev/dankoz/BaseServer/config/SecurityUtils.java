package dev.dankoz.BaseServer.config;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.util.CollectionUtils;
public class SecurityUtils {

    private static final String AUTHORITIES_CLAIM_NAME = "permissions";  // Or use "authorities" based on your JWT

    public static Collection<GrantedAuthority> extractAuthorityFromClaims(Map<String, Object> claims) {
        List<String> permissions = Arrays.stream(String.valueOf(claims.get(AUTHORITIES_CLAIM_NAME)).split(" ")).toList();

        if (CollectionUtils.isEmpty(permissions)) {
            return Set.of();  // Return empty list if no roles are found
        }

        return permissions.stream()
                .map(SimpleGrantedAuthority::new)  // Map roles to authorities
                .collect(Collectors.toSet());
    }
}