package uk.co.lucyleach.monzo_transaction_reader;

import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.DataStoreCredentialRefreshListener;
import com.google.api.client.auth.oauth2.StoredCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.util.store.DataStoreFactory;
import com.google.api.client.util.store.FileDataStoreFactory;

import java.io.File;
import java.io.IOException;

import static com.google.common.collect.Lists.newArrayList;

/**
 * User: Lucy
 * Date: 28/07/2018
 * Time: 20:57
 */
class CredentialLoader {
  private final HttpTransport httpTransport;
  private final JsonFactory jsonFactory;
  private final DataStoreFactory dataStoreFactory;

  CredentialLoader(HttpTransport httpTransport, JsonFactory jsonFactory, String pathToDataStore) throws IOException {
    this.httpTransport = httpTransport;
    this.jsonFactory = jsonFactory;
    this.dataStoreFactory = createFileDataStoreFactory(pathToDataStore);
  }

  Credential load(String clientId, String clientSecret) throws IOException {
    var dataStore = StoredCredential.getDefaultDataStore(dataStoreFactory);
    var dataStoreKey = "lleach-monzo-credentials";
    var storedCredential = dataStore.get(dataStoreKey);
    var refreshToken = storedCredential.getRefreshToken();
    var accessToken = storedCredential.getAccessToken();

    var credential = new GoogleCredential.Builder()
        .setTransport(httpTransport)
        .setJsonFactory(jsonFactory)
        .setClientAuthentication(new ClientParametersAuthentication(clientId, clientSecret))
        .setTokenServerUrl(new GenericUrl("https://api.monzo.com/oauth2/token"))
        .setRefreshListeners(newArrayList(new DataStoreCredentialRefreshListener(dataStoreKey, dataStore)))
        .build();
    credential.setRefreshToken(refreshToken);
    credential.setAccessToken(accessToken);

    return credential;
  }

  private static DataStoreFactory createFileDataStoreFactory(String pathToDataStore) throws IOException {
    return new FileDataStoreFactory(new File(pathToDataStore));
  }
}
