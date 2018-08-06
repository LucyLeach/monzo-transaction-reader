package uk.co.lucyleach.monzo_transaction_reader;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import uk.co.lucyleach.monzo_transaction_reader.monzo_model.TransactionList;

import java.io.IOException;

/**
 * User: Lucy
 * Date: 28/07/2018
 * Time: 21:05
 */
class TransactionLoader {
  private final HttpTransport httpTransport;
  private final JsonFactory jsonFactory;

  TransactionLoader(HttpTransport httpTransport, JsonFactory jsonFactory) {
    this.httpTransport = httpTransport;
    this.jsonFactory = jsonFactory;
  }

  TransactionList load(Credential credential, String accountId) throws IOException {
    var transactionUrl = "https://api.monzo.com/transactions";
    var requestFactory = httpTransport.createRequestFactory(request -> {
      credential.initialize(request);
      request.setParser(new JsonObjectParser(jsonFactory));
    });
    var transactionsUrlObject = new GenericUrl(transactionUrl);
    transactionsUrlObject.set("account_id", accountId);
    transactionsUrlObject.set("expand[]", "merchant");
    var request = requestFactory.buildGetRequest(transactionsUrlObject);
    var response = request.execute();
    return response.parseAs(TransactionList.class);
  }
}
