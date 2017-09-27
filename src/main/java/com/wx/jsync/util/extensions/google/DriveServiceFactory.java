package com.wx.jsync.util.extensions.google;

import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.extensions.java6.auth.oauth2.VerificationCodeReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.oauth2.Oauth2;
import com.google.api.services.oauth2.Oauth2Scopes;
import com.google.api.services.oauth2.model.Userinfoplus;
import com.wx.jsync.util.DesktopUtils;
import com.wx.util.Format;
import com.wx.util.log.LogHelper;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import static com.wx.jsync.Constants.APPLICATION_NAME;

/**
 * Factory for the {@link Drive} service that handles loading/creating credentials
 */
public class DriveServiceFactory {

    private static final Logger LOG = LogHelper.getLogger(DriveServiceFactory.class);

    private static final List<String> SCOPES = Arrays.asList(DriveScopes.DRIVE, Oauth2Scopes.USERINFO_PROFILE);
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    private static FileDataStoreFactory DATA_STORE_FACTORY;
    private static HttpTransport HTTP_TRANSPORT;
    private static String userId;
    private static java.io.File dataDirectory;


    public static void init(java.io.File dataDirectory) throws GeneralSecurityException, IOException {
        init(dataDirectory, "user");
    }

    public static void init(java.io.File dataDirectory, String userId) throws GeneralSecurityException, IOException {
        DriveServiceFactory.userId = userId;
        DriveServiceFactory.dataDirectory = dataDirectory;

        HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        DATA_STORE_FACTORY = new FileDataStoreFactory(dataDirectory);
    }

    public static boolean isInit() {
        return userId != null && dataDirectory != null && HTTP_TRANSPORT != null && DATA_STORE_FACTORY != null;
    }

    /**
     * Build and return an authorized Drive client service.
     *
     * @return an authorized Drive client service
     *
     * @throws IOException
     */
    public static Drive getDriveService(boolean authorize) throws IOException {
        ensureIsInit();

        Credential credential = getCredential(authorize);

        return new Drive.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    public static boolean removeCredentials() {
        ensureIsInit();

        boolean changed = false;

        java.io.File[] files = dataDirectory.listFiles();
        if (files != null) {
            for (java.io.File file : files) {
                if (file.delete()) {
                    changed = true;
                }
            }
        }

        return changed;
    }

    public static Userinfoplus getUserInfo(boolean authorize) throws IOException {
        ensureIsInit();

        Oauth2 auth = new Oauth2.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredential(authorize))
                .setApplicationName(APPLICATION_NAME)
                .build();
        return auth.userinfo().get().execute();
    }

    /**
     * Creates an authorized Credential object.
     *
     * @return an authorized Credential object.
     *
     * @throws IOException
     */
    private static Credential getCredential(boolean authorizeIfNeeded) throws IOException {
        // Load client secrets.
        //noinspection SpellCheckingInspection
        InputStream in =
                DriveServiceFactory.class.getResourceAsStream("/google/client_secret.json");
        GoogleClientSecrets clientSecrets =
                GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow =
                new GoogleAuthorizationCodeFlow.Builder(
                        HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                        .setDataStoreFactory(DATA_STORE_FACTORY)
                        .setAccessType("offline")
                        .build();

        Credential credential = loadCredential(flow);
        if (credential == null) {
            if (authorizeIfNeeded) {
                credential = authorize(flow);
            } else {
                throw new IOException("Unauthorized user");
            }
        }

        assert credential != null;
        LOG.finer("Credential loaded, expires in " + Format.formatTime(credential.getExpirationTimeMilliseconds()));
        return credential;
    }

    private static Credential loadCredential(GoogleAuthorizationCodeFlow flow) throws IOException {
        Credential credential = flow.loadCredential(userId);
        if (credential != null
                && (credential.getRefreshToken() != null || credential.getExpiresInSeconds() > 60)) {
            return credential;
        }
        return null;
    }

    private static Credential authorize(GoogleAuthorizationCodeFlow flow) throws IOException {
        VerificationCodeReceiver receiver = new CustomCodeReceiver();

        try {
            // openInvoice in browser
            String redirectUri = receiver.getRedirectUri();
            AuthorizationCodeRequestUrl authorizationUrl =
                    flow.newAuthorizationUrl().setRedirectUri(redirectUri);
            DesktopUtils.openUrl(authorizationUrl.build());
            // receive authorization code and exchange it for an access token
            String code = receiver.waitForCode();
            TokenResponse response = flow.newTokenRequest(code).setRedirectUri(redirectUri).execute();
            // store credential and return it
            return flow.createAndStoreCredential(response, userId);
        } finally {
            try {
                receiver.stop();
            } catch (IOException e) {
                /* no-op */
            }
        }
    }


    private static void ensureIsInit() {
        if (!isInit()) {
            throw new IllegalStateException("Must init first");
        }
    }

}