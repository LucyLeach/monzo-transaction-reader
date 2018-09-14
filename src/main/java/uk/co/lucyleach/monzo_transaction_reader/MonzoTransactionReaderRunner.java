package uk.co.lucyleach.monzo_transaction_reader;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import uk.co.lucyleach.monzo_transaction_reader.processor.ClientProcessingDetails;
import uk.co.lucyleach.monzo_transaction_reader.processor.TransactionProcessor;
import uk.co.lucyleach.monzo_transaction_reader.report.ReportCreator;
import uk.co.lucyleach.monzo_transaction_reader.report.excel_writer.ReportWriter_Excel;

import java.io.IOException;
import java.util.Optional;

/**
 * User: Lucy
 * Date: 22/07/2018
 * Time: 10:24
 */
public class MonzoTransactionReaderRunner {
  private final ClientAccountDetailsReader clientAccountDetailsReader;
  private final CredentialLoader credentialLoader;
  private final TransactionLoader transactionLoader;
  private final TransactionProcessor transactionProcessor;
  private final ReportCreator reportCreator;
  private final ReportWriter_Excel reportWriter;

  private MonzoTransactionReaderRunner(String pathToDataStore, String pathToOutput) throws IOException {
    HttpTransport httpTransport = new NetHttpTransport();
    JsonFactory jsonFactory = new JacksonFactory();

    this.clientAccountDetailsReader = new ClientAccountDetailsReader();
    this.credentialLoader = new CredentialLoader(httpTransport, jsonFactory, pathToDataStore);
    this.transactionLoader = new TransactionLoader(httpTransport, jsonFactory);
    this.transactionProcessor = new TransactionProcessor();
    this.reportCreator = new ReportCreator();
    this.reportWriter = new ReportWriter_Excel(pathToOutput);
  }

  private void run(String pathToProps, Optional<String> sinceDateOpt) throws Exception {
    var props = clientAccountDetailsReader.read(pathToProps);
    var credential = credentialLoader.load(props.getClientId(), props.getClientSecret());
    var transactionList = transactionLoader.load(credential, props.getAccountId(), sinceDateOpt);

    var clientProcessingDetails = ClientProcessingDetails.builder()
        .addPotsToRecogniseIn(props.getPotsToRecogniseIn())
        .addPotsToRecogniseOut(props.getPotsToRecogniseOut())
        .addAutoTagMerchants(props.getAutoTagMerchants())
        .addAutoTagAccounts(props.getAutoTagAccounts())
        .build();
    var processorResult = transactionProcessor.process(transactionList, clientProcessingDetails);
    if(processorResult.getUnsuccessfulResults().isEmpty()) {
      var report = reportCreator.create(processorResult);
      reportWriter.write(report);
      var debug = 1;
    } else {
      throw new Exception("Some transactions did not process");
    }
  }

  public static void main(String[] args) throws Exception {
    if(args.length < 4 || args[0] == null || args[1] == null || args[2] == null) {
      throw new Exception("Need to supply three arguments: path to properties file, path to credential store, path for output.  Optional: date to load transactions since");
    }

    var pathToProps = args[0];
    var pathToCredentialDataStore = args[1];
    var pathToOutput = args[2];
    var sinceDateStringOpt = Optional.ofNullable(args[3]);

    var runner = new MonzoTransactionReaderRunner(pathToCredentialDataStore, pathToOutput);
    runner.run(pathToProps, sinceDateStringOpt);
  }
}
