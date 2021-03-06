package uk.co.lucyleach.monzo_transaction_reader.monzo_model;

import com.google.api.client.util.Key;

/**
 * User: Lucy
 * Date: 22/07/2018
 * Time: 20:16
 */
@SuppressWarnings("WeakerAccess") //Class must be public to be used by Jackson
public class WhoAmIResponse {
  @Key
  private boolean authenticated;

  @Key("client_id")
  private String clientId;

  @Key("user_id")
  private String userId;
}
