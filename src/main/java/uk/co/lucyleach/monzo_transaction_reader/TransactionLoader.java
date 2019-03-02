package uk.co.lucyleach.monzo_transaction_reader;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import uk.co.lucyleach.monzo_transaction_reader.monzo_model.TransactionList;

import java.io.IOException;
import java.util.Optional;

/**
 * User: Lucy
 * Date: 28/07/2018
 * Time: 21:05
 */
class TransactionLoader extends ApiLoader<TransactionList> {
  TransactionLoader(HttpTransport httpTransport, JsonFactory jsonFactory) {
    super(httpTransport, jsonFactory, "https://api.monzo.com/transactions");
  }

  TransactionList load(Credential credential, String accountId, Optional<String> sinceDateOpt) throws IOException {
    return super.load(credential, urlObject -> {
      urlObject.set("account_id", accountId);
      urlObject.set("expand[]", "merchant");
      sinceDateOpt.ifPresent(s -> urlObject.set("since", s));
    });
  }

  @Override
  Class<TransactionList> getReturnType() {
    return TransactionList.class;
  }
}
