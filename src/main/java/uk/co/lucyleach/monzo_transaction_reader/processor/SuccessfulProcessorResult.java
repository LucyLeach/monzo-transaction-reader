package uk.co.lucyleach.monzo_transaction_reader.processor;

import uk.co.lucyleach.monzo_transaction_reader.monzo_model.Transaction;
import uk.co.lucyleach.monzo_transaction_reader.output_model.ProcessedTransaction;
import uk.co.lucyleach.monzo_transaction_reader.utils.Pair;

import java.util.Collection;

/**
 * User: Lucy
 * Date: 29/07/2018
 * Time: 21:27
 */
class SuccessfulProcessorResult extends Pair<Transaction, Collection<? extends ProcessedTransaction>> {
  SuccessfulProcessorResult(Transaction transaction, Collection<? extends ProcessedTransaction> processedTransactions) {
    super(transaction, processedTransactions);
  }

  Transaction getOriginalTransaction() {
    return getA();
  }

  Collection<? extends ProcessedTransaction> getProcessedTransactions() {
    return getB();
  }
}
