package uk.co.lucyleach.monzo_transaction_reader;

import com.google.api.client.util.Key;

/**
 * User: Lucy
 * Date: 22/07/2018
 * Time: 20:39
 */
@SuppressWarnings("WeakerAccess") //Class must be public to be used by Jackson
public class Transaction
{
  @Key
  private String id;

  @Key
  private int amount; //NB pence, -ve if money removed from account

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
}
