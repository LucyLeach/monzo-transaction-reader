package uk.co.lucyleach.monzo_transaction_reader.processor;

import uk.co.lucyleach.monzo_transaction_reader.monzo_model.Transaction;
import uk.co.lucyleach.monzo_transaction_reader.output_model.ProcessedTransaction;

import java.util.Collection;

/**
 * User: Lucy
 * Date: 29/07/2018
 * Time: 21:27
 */
class SuccessfulProcessorResult extends Pair<Transaction, Collection<ProcessedTransaction>>
{
  protected SuccessfulProcessorResult(Transaction transaction, Collection<ProcessedTransaction> processedTransactions)
  {
    super(transaction, processedTransactions);
  }

  public Transaction getOriginalTransaction()
  {
    return getA();
  }

  public Collection<ProcessedTransaction> getProcessedTransactions()
  {
    return getB();
  }
}
