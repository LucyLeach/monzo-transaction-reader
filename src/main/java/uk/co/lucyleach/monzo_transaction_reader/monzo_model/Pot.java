package uk.co.lucyleach.monzo_transaction_reader.monzo_model;

import com.google.api.client.util.Key;

/**
 * User: Lucy
 * Date: 02/03/2019
 * Time: 14:18
 */
public class Pot {
  @Key
  private String id;

  @Key
  private String name;

  public Pot() {
  }

  public Pot(String id, String name) {
    this.id = id;
    this.name = name;
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  @Override
  public String toString() {
    return "Pot{" +
        "id='" + id + '\'' +
        ", name='" + name + '\'' +
        '}';
  }
}
