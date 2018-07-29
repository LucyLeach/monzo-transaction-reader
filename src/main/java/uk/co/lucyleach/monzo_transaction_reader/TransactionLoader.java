package uk.co.lucyleach.monzo_transaction_reader;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.*;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import uk.co.lucyleach.monzo_transaction_reader.monzo_model.TransactionList;

import java.io.IOException;

/**
 * User: Lucy
 * Date: 28/07/2018
 * Time: 21:05
 */
class TransactionLoader
{
  private final HttpTransport httpTransport;
  private final JsonFactory jsonFactory;

  TransactionLoader(HttpTransport httpTransport, JsonFactory jsonFactory)
  {
    this.httpTransport = httpTransport;
    this.jsonFactory = jsonFactory;
  }

  TransactionList load(Credential credential, String accountId) throws IOException
  {
    String transactionUrl = "https://api.monzo.com/transactions";
    HttpRequestFactory requestFactory = httpTransport.createRequestFactory(request -> {
      credential.initialize(request);
      request.setParser(new JsonObjectParser(jsonFactory));
    });
    GenericUrl transactionsUrlObject = new GenericUrl(transactionUrl);
    transactionsUrlObject.set("account_id", accountId);
    transactionsUrlObject.set("expand[]", "merchant");
    HttpRequest request = requestFactory.buildGetRequest(transactionsUrlObject);
    HttpResponse response = request.execute();
    return response.parseAs(TransactionList.class);
  }
}
