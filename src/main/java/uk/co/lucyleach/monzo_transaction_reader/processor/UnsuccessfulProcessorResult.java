package uk.co.lucyleach.monzo_transaction_reader.processor;

import uk.co.lucyleach.monzo_transaction_reader.monzo_model.Transaction;
import uk.co.lucyleach.monzo_transaction_reader.utils.Pair;

/**
 * User: Lucy
 * Date: 29/07/2018
 * Time: 21:28
 */
class UnsuccessfulProcessorResult extends Pair<Transaction, String> {
  UnsuccessfulProcessorResult(Transaction transaction, String message) {
    super(transaction, message);
  }

  Transaction getOriginalTransaction() {
    return getA();
  }
}
