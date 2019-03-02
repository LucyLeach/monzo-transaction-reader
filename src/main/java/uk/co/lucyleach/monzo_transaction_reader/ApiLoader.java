package uk.co.lucyleach.monzo_transaction_reader;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;

import java.io.IOException;
import java.util.function.Consumer;

/**
 * User: Lucy
 * Date: 02/03/2019
 * Time: 14:23
 */
public abstract class ApiLoader<T> {
  private final HttpTransport httpTransport;
  private final JsonFactory jsonFactory;
  private final String url;

  protected ApiLoader(HttpTransport httpTransport, JsonFactory jsonFactory, String url) {
    this.httpTransport = httpTransport;
    this.jsonFactory = jsonFactory;
    this.url = url;
  }

  abstract Class<T> getReturnType();

  T load(Credential credential, Consumer<GenericUrl> parameterAdder) throws IOException {
    var requestFactory = httpTransport.createRequestFactory(request -> {
      credential.initialize(request);
      request.setParser(new JsonObjectParser(jsonFactory));
    });
    var urlObject = new GenericUrl(url);
    parameterAdder.accept(urlObject);
    var request = requestFactory.buildGetRequest(urlObject);
    var response = request.execute();
    return response.parseAs(getReturnType());
  }
}
