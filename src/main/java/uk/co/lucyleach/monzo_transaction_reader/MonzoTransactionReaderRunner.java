package uk.co.lucyleach.monzo_transaction_reader;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;

import java.io.IOException;

/**
 * User: Lucy
 * Date: 22/07/2018
 * Time: 10:24
 */
public class MonzoTransactionReaderRunner
{
  private final ClientAccountDetailsReader clientAccountDetailsReader;
  private final CredentialLoader credentialLoader;
  private final TransactionLoader transactionLoader;

  private MonzoTransactionReaderRunner(String pathToDataStore) {
    HttpTransport httpTransport = new NetHttpTransport();
    JsonFactory jsonFactory = new JacksonFactory();

    this.clientAccountDetailsReader = new ClientAccountDetailsReader();
    this.credentialLoader = new CredentialLoader(httpTransport, jsonFactory, pathToDataStore);
    this.transactionLoader = new TransactionLoader(httpTransport, jsonFactory);
  }

  MonzoTransactionReaderRunner(ClientAccountDetailsReader clientAccountDetailsReader, CredentialLoader credentialLoader, TransactionLoader transactionLoader)
  {
    this.clientAccountDetailsReader = clientAccountDetailsReader;
    this.credentialLoader = credentialLoader;
    this.transactionLoader = transactionLoader;
  }

  public static void main(String[] args) throws Exception
  {
    if(args.length < 2 || args[0] == null || args[1] == null) {
      throw new Exception("Need to supply two arguments: path to properties file, path to credential store");
    }

    String pathToProps = args[0];
    String pathToCredentialDataStore = args[1];

    MonzoTransactionReaderRunner runner = new MonzoTransactionReaderRunner(pathToCredentialDataStore);
    runner.run(pathToProps);
  }

  private void run(String pathToProps) throws IOException
  {
    ClientAccountDetails props = clientAccountDetailsReader.read(pathToProps);
    Credential credential = credentialLoader.load(props.getClientId(), props.getClientSecret());
    TransactionList transactionList = transactionLoader.load(credential, props.getAccountId());
    int debug = 1;
  }
}
