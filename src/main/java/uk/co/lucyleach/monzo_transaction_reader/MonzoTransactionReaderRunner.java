package uk.co.lucyleach.monzo_transaction_reader;

import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.StoredCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.DataStore;
import com.google.api.client.util.store.FileDataStoreFactory;

import java.io.File;
import java.util.Properties;

/**
 * User: Lucy
 * Date: 22/07/2018
 * Time: 10:24
 */
public class MonzoTransactionReaderRunner
{

  public static void main(String[] args) throws Exception
  {
    if(args.length < 2 || args[0] == null || args[1] == null) {
      throw new Exception("Need to supply two arguments: path to properties file, path to credential store");
    }

    String pathToProps = args[0];
    String pathToCredentialStore = args[1];

    PropertiesReader_File propsReader = new PropertiesReader_File();
    Properties props = propsReader.readProperties(pathToProps);
    String clientId = props.getProperty("client_id");
    String clientSecret = props.getProperty("client_secret");

    FileDataStoreFactory dataStoreFactory = new FileDataStoreFactory(new File(pathToCredentialStore));
    DataStore<StoredCredential> dataStore = StoredCredential.getDefaultDataStore(dataStoreFactory);
    String dataStoreKey = "lleach-monzo-credentials";
    StoredCredential storedCredential = dataStore.get(dataStoreKey);
    String refreshToken = storedCredential.getRefreshToken();
    String accessToken = storedCredential.getAccessToken();

    Credential credential = new GoogleCredential.Builder()
        .setTransport(new NetHttpTransport())
        .setJsonFactory(new JacksonFactory())
        .setClientAuthentication(new ClientParametersAuthentication(clientId, clientSecret))
        .setTokenServerUrl(new GenericUrl("https://api.monzo.com/oauth2/token"))
        .build();
    credential.setRefreshToken(refreshToken);
    credential.setAccessToken(accessToken);

    try
    {
      credential.refreshToken();
      System.out.println("New refresh token: " + credential.getRefreshToken());
    } finally
    {
      dataStore.set(dataStoreKey, new StoredCredential(credential));
    }
  }
}
