package uk.co.lucyleach.monzo_transaction_reader.monzo_model;

import com.google.api.client.util.Key;

/**
 * User: Lucy
 * Date: 22/07/2018
 * Time: 20:39
 */
@SuppressWarnings("WeakerAccess") //Class must be public to be used by Jackson
public class Transaction {
  @Key
  private String id;

  @Key
  private int amount; //NB pence, -ve if money removed from account

  @Key
  private String currency;

  @Key
  private String created;

  @Key
  private String notes;

  @Key
  private Merchant merchant;

  @Key
  private String description;

  @Key
  private Counterparty counterparty;

  public Transaction(String id, int amount, String currency, String created, String notes, Merchant merchant, String description, Counterparty counterparty) {
    this.id = id;
    this.amount = amount;
    this.currency = currency;
    this.created = created;
    this.notes = notes;
    this.merchant = merchant;
    this.description = description;
    this.counterparty = counterparty;
  }

  public Transaction() {
  }

  public String getId() {
    return id;
  }

  public int getAmount() {
    return amount;
  }

  public String getCurrency() {
    return currency;
  }

  public String getCreated() {
    return created;
  }

  public String getNotes() {
    return notes;
  }

  public Merchant getMerchant() {
    return merchant;
  }

  public String getDescription() {
    return description;
  }

  public Counterparty getCounterparty() {
    return counterparty;
  }

  @Override
  public String toString() {
    return "Transaction{" +
        "id='" + id + '\'' +
        ", amount=" + amount +
        ", currency='" + currency + '\'' +
        ", created='" + created + '\'' +
        ", notes='" + notes + '\'' +
        ", merchant=" + merchant +
        ", description='" + description + '\'' +
        ", counterparty=" + counterparty +
        '}';
  }
}
