package dev.dankoz.BaseServer.google.auth;

import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.*;
import java.security.GeneralSecurityException;
import java.util.Set;
import java.util.stream.Collectors;


@Service
public class GoogleOAuth2Service {

    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String CREDENTIALS_FILE_PATH = "/google/credentials.json";
    private static final String REDIRECT_URL = "http://localhost:3000/auth/google/callback";

    private static GoogleAuthorizationCodeFlow getFlow(Set<String> scopes) throws IOException, GeneralSecurityException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        InputStream in = GoogleOAuth2Service.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets =
                GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        return new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT,
                JSON_FACTORY,
                clientSecrets, scopes)                // The scopes you're requesting access to (e.g., calendar, email, etc.)// Store the tokens in a file (local storage)
                .setAccessType("offline")
                .setApprovalPrompt("force")// Request offline access to get a refresh token
               // Force the consent screen to appear every time (helps get the refresh token)
                .build();
    }
    public ResponseEntity<String> getLoginURL(GoogleOAuth2Request request) throws IOException, GeneralSecurityException {

        GoogleAuthorizationCodeFlow flow = getFlow(request.scopes().stream().map(e -> ScopeType.scopeFromString(e).getScope()).collect(Collectors.toSet()));
        return ResponseEntity.ok(flow.newAuthorizationUrl().setRedirectUri(REDIRECT_URL).build());
    }

    public  ResponseEntity<String> saveTokens(GoogleOAuth2Request request) throws GeneralSecurityException, IOException {
        GoogleAuthorizationCodeFlow flow = getFlow(request.scopes());

        // Exchange the authorization code for access and refresh tokens
        TokenResponse tokenResponse = flow.newTokenRequest(request.code())
                .setRedirectUri(REDIRECT_URL)  // Your redirect URI
                .execute();


        // Print access token and refresh token
        String accessToken = tokenResponse.getAccessToken();
        String refreshToken = tokenResponse.getRefreshToken();

        return ResponseEntity.ok("Tokens received successfully!");
    }


}
