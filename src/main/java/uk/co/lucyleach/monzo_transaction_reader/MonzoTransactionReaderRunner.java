package uk.co.lucyleach.monzo_transaction_reader;

import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;

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
    if(args.length == 0 || args[0] == null) {
      throw new Exception("Need to supply one argument: path to properties file");
    }

    String pathToProps = args[0];
    PropertiesReader_File propsReader = new PropertiesReader_File();
    Properties props = propsReader.readProperties(pathToProps);
    String clientId = props.getProperty("client_id");
    String clientSecret = props.getProperty("client_secret");
    String refreshToken = props.getProperty("refresh_token");

    Credential credential = new GoogleCredential.Builder()
        .setTransport(new NetHttpTransport())
        .setJsonFactory(new JacksonFactory())
        .setClientAuthentication(new ClientParametersAuthentication(clientId, clientSecret))
        .setTokenServerUrl(new GenericUrl("https://api.monzo.com/oauth2/token"))
        .build();
    credential.setRefreshToken(refreshToken);
    credential.refreshToken();
    System.out.println("New refresh token: " + credential.getRefreshToken());
    int debug = 1;
  }
}
