import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import com.google.api.services.drive.model.FileList

import groovy.transform.CompileStatic

import java.security.GeneralSecurityException

@CompileStatic
class DriveQuickstartGroovy {
    /** Application name. */
    private static final String APPLICATION_NAME = "Google Drive API Java Quickstart"
    /** Global instance of the JSON factory. */
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance()
    /** Directory to store authorization tokens for this application. */
    private static final String TOKENS_DIRECTORY_PATH = "tokens"

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE_METADATA_READONLY)
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json"

    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) {
        // Load client secrets.
        InputStream inp = DriveQuickstartGroovy.class.getResourceAsStream(CREDENTIALS_FILE_PATH)
        if (inp == null) {
            throw new FileNotFoundException('Resource not found: ' + CREDENTIALS_FILE_PATH)
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(inp))

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build()
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build()
        Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user")
        //returns an authorized Credential object.
        return credential
    }

    public static void main(String... args) {
        // Build a new authorized API client service.
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport()
        Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build()

        // Print the names and IDs for up to 10 files.
        FileList result = service.files().list()
                .setPageSize(100)
                .setFields("nextPageToken, files(id, name, properties, mimeType, parents)")
                .execute()
        List<File> files = result.files
        if (files == null || files.empty) {
            println "No files found."
        } else {
            println "Files:"
            files.eachWithIndex {
                File file, int i
                ->
                println "${i+1} $file"
               // println "\t${file.name} (${file.id}) --- $file.properties"
            }
        }
    }

}