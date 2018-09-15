package uk.co.lucyleach.monzo_transaction_reader.monzo_model;

import com.google.api.client.util.Key;

/**
 * User: Lucy
 * Date: 22/07/2018
 * Time: 20:50
 */
@SuppressWarnings("WeakerAccess") //Class must be public to be used by Jackson
public class Merchant {
  @Key
  private String name;

  public Merchant(String name) {
    this.name = name;
  }

  public Merchant() {
  }

  public String getName() {
    return name;
  }

  @Override
  public String toString() {
    return "Merchant{" +
        "name='" + name + '\'' +
        '}';
  }
}
