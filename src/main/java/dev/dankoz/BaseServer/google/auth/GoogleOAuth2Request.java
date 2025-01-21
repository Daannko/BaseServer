package dev.dankoz.BaseServer.google.auth;

import java.util.Set;

public record GoogleOAuth2Request(Set<String> scopes,String code) {
}
