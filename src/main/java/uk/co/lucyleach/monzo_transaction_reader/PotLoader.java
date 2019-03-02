package uk.co.lucyleach.monzo_transaction_reader;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import uk.co.lucyleach.monzo_transaction_reader.monzo_model.PotList;

import java.io.IOException;

/**
 * User: Lucy
 * Date: 02/03/2019
 * Time: 14:17
 */
public class PotLoader extends ApiLoader<PotList> {
  public PotLoader(HttpTransport httpTransport, JsonFactory jsonFactory) {
    super(httpTransport, jsonFactory, "https://api.monzo.com/pots");
  }

  @Override
  Class<PotList> getReturnType() {
    return PotList.class;
  }

  PotList load(Credential credential) throws IOException {
    return super.load(credential, genericUrl -> {});
  }
}
