package dev.dankoz.BaseServer.google.auth;

import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;

import java.util.List;

public enum ScopeType {
    CALENDAR(CalendarScopes.CALENDAR),
    CALENDAR_READ(CalendarScopes.CALENDAR_READONLY),
    CALENDAR_EVENTS(CalendarScopes.CALENDAR_EVENTS);
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
            case "calendar" -> ScopeType.CALENDAR;
            case "calendar-read" -> ScopeType.CALENDAR_READ;
            case "calendar-events" -> ScopeType.CALENDAR_EVENTS;

            default -> throw new IllegalStateException("Unexpected value: " + scopeString);
        };
    }
}
