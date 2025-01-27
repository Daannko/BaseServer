package dev.dankoz.BaseServer.google.auth;

import dev.dankoz.BaseServer.todo.dto.AddToDoRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.GeneralSecurityException;

@RestController()
@RequestMapping("/google")
public class GoogleOAuth2Controller {

    private final GoogleOAuth2Service googleCalendarService;

    public GoogleOAuth2Controller(GoogleOAuth2Service googleCalendarService) {
        this.googleCalendarService = googleCalendarService;
    }

    @PostMapping("/auth")
    public ResponseEntity<?> login(@RequestBody GoogleOAuth2Request request) throws GeneralSecurityException, IOException {
        return googleCalendarService.getLoginURL(request);
    }

    @PostMapping("/callback")
    public ResponseEntity<?> handleOAuth2Callback(@RequestBody GoogleOAuth2Request request) throws Exception {
        return googleCalendarService.saveTokens(request);
    }

    @GetMapping("/accounts")
    public ResponseEntity<?> handleOAuth2Callback() throws Exception {
        return googleCalendarService.getMyAccounts();
    }

}
