package uk.co.lucyleach.monzo_transaction_reader.monzo_model;

import com.google.api.client.util.Key;

import java.util.List;

/**
 * User: Lucy
 * Date: 22/07/2018
 * Time: 20:41
 */
public class TransactionList {
  @Key
  private List<Transaction> transactions;

  public TransactionList(Transaction... transactions) {
    this(List.of(transactions));
  }

  public TransactionList(List<Transaction> transactions) {
    this.transactions = transactions;
  }

  public TransactionList() {
  }

  public List<Transaction> getTransactions() {
    return transactions == null ? List.of() : List.copyOf(transactions);
  }
}
