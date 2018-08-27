package uk.co.lucyleach.monzo_transaction_reader.processor;

import uk.co.lucyleach.monzo_transaction_reader.monzo_model.Transaction;

/**
 * User: Lucy
 * Date: 27/08/2018
 * Time: 13:24
 */
class ParsingException extends Exception {
  private final Transaction transaction;

  ParsingException(String message) {
    super(message);
    this.transaction = null;
  }

  ParsingException(String message, Transaction transaction) {
    super(message);
    this.transaction = transaction;
  }

  public ParsingException changeTransaction(Transaction transaction) {
    return new ParsingException(getMessage(), transaction);
  }

  public Transaction getTransaction() {
    return transaction;
  }
}
