package dev.dankoz.BaseServer.config;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.util.CollectionUtils;
public class SecurityUtils {

    private static final String AUTHORITIES_CLAIM_NAME = "permissions";  // Or use "authorities" based on your JWT

    public static Collection<GrantedAuthority> extractAuthorityFromClaims(Map<String, Object> claims) {
        List<String> permissions = (List<String>) claims.get(AUTHORITIES_CLAIM_NAME);

        if (CollectionUtils.isEmpty(permissions)) {
            return Set.of();  // Return empty list if no roles are found
        }

        return permissions.stream()
                .map(permission -> new SimpleGrantedAuthority("PERMISSION_" + permission))  // Map roles to authorities
                .collect(Collectors.toSet());
    }
}