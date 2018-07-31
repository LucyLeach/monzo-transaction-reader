package uk.co.lucyleach.monzo_transaction_reader;

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

  private MonzoTransactionReaderRunner(String pathToDataStore) throws IOException
  {
    HttpTransport httpTransport = new NetHttpTransport();
    JsonFactory jsonFactory = new JacksonFactory();

    this.clientAccountDetailsReader = new ClientAccountDetailsReader();
    this.credentialLoader = new CredentialLoader(httpTransport, jsonFactory, pathToDataStore);
    this.transactionLoader = new TransactionLoader(httpTransport, jsonFactory);
  }

  public static void main(String[] args) throws Exception
  {
    if(args.length < 2 || args[0] == null || args[1] == null) {
      throw new Exception("Need to supply two arguments: path to properties file, path to credential store");
    }

    var pathToProps = args[0];
    var pathToCredentialDataStore = args[1];

    var runner = new MonzoTransactionReaderRunner(pathToCredentialDataStore);
    runner.run(pathToProps);
  }

  private void run(String pathToProps) throws IOException
  {
    var props = clientAccountDetailsReader.read(pathToProps);
    var credential = credentialLoader.load(props.getClientId(), props.getClientSecret());
    var transactionList = transactionLoader.load(credential, props.getAccountId());
    var debug = 1;
  }
}
