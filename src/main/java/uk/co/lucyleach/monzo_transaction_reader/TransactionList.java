package uk.co.lucyleach.monzo_transaction_reader;

import com.google.api.client.util.Key;

import java.util.List;

/**
 * User: Lucy
 * Date: 22/07/2018
 * Time: 20:41
 */
@SuppressWarnings("WeakerAccess") //Class must be public to be used by Jackson
public class TransactionList
{
  @Key
  private List<Transaction> transactions;
}
