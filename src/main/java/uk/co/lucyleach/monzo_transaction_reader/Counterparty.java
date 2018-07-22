package uk.co.lucyleach.monzo_transaction_reader;

import com.google.api.client.util.Key;

/**
 * User: Lucy
 * Date: 22/07/2018
 * Time: 20:58
 */
@SuppressWarnings("WeakerAccess") //Class must be public to be used by Jackson
public class Counterparty
{
  @Key("account_number")
  private int accountNumber;

  @Key("sort_code")
  private int sortCode;
}
