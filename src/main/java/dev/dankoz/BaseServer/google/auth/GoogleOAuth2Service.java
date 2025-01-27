package dev.dankoz.BaseServer.google.auth;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.oauth2.Oauth2;
import com.google.api.services.oauth2.model.Userinfo;
import dev.dankoz.BaseServer.general.model.User;
import dev.dankoz.BaseServer.general.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.*;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class GoogleOAuth2Service {

    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String CREDENTIALS_FILE_PATH = "/google/credentials.json";
    private static final String REDIRECT_URL = "http://localhost:3000/auth/google/callback";
    private final GoogleUserRepository googleUserRepository;
    private final UserService userService;

    public GoogleOAuth2Service(GoogleUserRepository googleUserRepository, UserService userService) {
        this.googleUserRepository = googleUserRepository;
        this.userService = userService;
    }

    private static GoogleAuthorizationCodeFlow getFlow(Set<String> scopes) throws IOException, GeneralSecurityException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        InputStream in = GoogleOAuth2Service.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets =
                GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        return new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT,
                JSON_FACTORY,
                clientSecrets, scopes)
                .setAccessType("offline")
                .setApprovalPrompt("force")
                .build();
    }
    public ResponseEntity<String> getLoginURL(GoogleOAuth2Request request) throws IOException, GeneralSecurityException {

        GoogleAuthorizationCodeFlow flow = getFlow(request.scopes().stream().map(e -> ScopeType.scopeFromString(e).getScope()).collect(Collectors.toSet()));
        return ResponseEntity.ok(flow.newAuthorizationUrl()
                    .set("include_granted_scopes",true)
                .setRedirectUri(REDIRECT_URL)
                .build());
    }

    public  ResponseEntity<?> saveTokens(GoogleOAuth2Request request) throws GeneralSecurityException, IOException {
        GoogleAuthorizationCodeFlow flow = getFlow(request.scopes());

        TokenResponse tokenResponse = flow.newTokenRequest(request.code())
                .setRedirectUri(REDIRECT_URL)
                .execute();

        Credential credential = flow.createAndStoreCredential(tokenResponse,null);

        Oauth2 oauth2 = new Oauth2.Builder(flow.getTransport(), flow.getJsonFactory(),credential).setApplicationName("Loa 2").build();
        Userinfo userinfo = oauth2.userinfo().get().execute();

        User user = userService.getUserByEmail(SecurityContextHolder.getContext().getAuthentication().getName());
        Optional<GoogleUser> accounts = googleUserRepository.getByUserAndGoogleMail(user, user.getEmail());

        GoogleUser googleUser;
        Set<ScopeType> scopes = Arrays.stream(tokenResponse.getScope()
                        .split(" "))
                .map(ScopeType::scopeFromString)
                .collect(Collectors.toSet());


        if(accounts.isEmpty()){
            googleUser = GoogleUser.builder()
                    .user(user)
                    .googleID(userinfo.getId())
                    .googleMail(userinfo.getEmail())
                    .scopes(scopes)
                    .token(tokenResponse.getAccessToken())
                    .refreshToken(tokenResponse.getRefreshToken())
                    .expires(new Date(System.currentTimeMillis() + tokenResponse.getExpiresInSeconds() - 10 * 1000))
                    .build();
        }else{
            googleUser = accounts.get();
            googleUser.setToken(tokenResponse.getAccessToken());
            googleUser.setRefreshToken(tokenResponse.getRefreshToken());
            googleUser.setExpires(new Date(System.currentTimeMillis() + tokenResponse.getExpiresInSeconds() - 10 * 1000));
            googleUser.addScopes(scopes);
        }

        googleUserRepository.save(googleUser);
        return ResponseEntity.ok(googleUser.getDTO());
    }

    public  ResponseEntity<?> getMyAccounts() throws GeneralSecurityException, IOException {
        User user = userService.getUserByEmail(SecurityContextHolder.getContext().getAuthentication().getName());
        List<GoogleUser> accounts = googleUserRepository.getAllByUser(user);
        return ResponseEntity.ok(accounts);
    }

}
