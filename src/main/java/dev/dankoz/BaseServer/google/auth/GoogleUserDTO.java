package dev.dankoz.BaseServer.google.auth;

import java.util.Date;
import java.util.Set;

public record GoogleUserDTO( Integer id,String googleMail, String googleId, Set<ScopeType> scopes, Date tokenExpires) {
}
