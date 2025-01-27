package dev.dankoz.BaseServer.google.auth;


import java.util.List;

public enum ScopeType {
    OPENID("openid"),
    EMAIL("https://www.googleapis.com/auth/userinfo.email"),
    PROFILE("https://www.googleapis.com/auth/userinfo.profile"),
    CALENDAR("https://www.googleapis.com/auth/calendar"),
    CALENDAR_READ("https://www.googleapis.com/auth/calendar.readonly"),
    CALENDAR_EVENTS("https://www.googleapis.com/auth/calendar.events");
//    GMAIL(GmailScopes.GMAIL_READONLY),
//    DRIVE(DriveScopes.DRIVE);

    private final String scope;

    ScopeType(String scope) {
        this.scope = scope;
    }
    public String getScope() {
        return scope;
    }

    public static ScopeType scopeFromString(String scopeString){
        return switch (scopeString){
            case "https://www.googleapis.com/auth/calendar","calendar" -> ScopeType.CALENDAR;
            case "calendar-read", "https://www.googleapis.com/auth/calendar.readonly" -> ScopeType.CALENDAR_READ;
            case "calendar-events" -> ScopeType.CALENDAR_EVENTS;
            case "email", "https://www.googleapis.com/auth/userinfo.email" -> ScopeType.EMAIL;
            case "https://www.googleapis.com/auth/userinfo.profile" -> ScopeType.PROFILE;
            case "openid" -> ScopeType.OPENID;

            default -> throw new IllegalStateException("Unexpected value: " + scopeString);
        };
    }
}
