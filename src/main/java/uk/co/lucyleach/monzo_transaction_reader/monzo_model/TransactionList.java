package uk.co.lucyleach.monzo_transaction_reader.monzo_model;

import com.google.api.client.util.Key;

import java.util.List;

/**
 * User: Lucy
 * Date: 22/07/2018
 * Time: 20:41
 */
public class TransactionList
{
  @Key
  private List<Transaction> transactions;
}
