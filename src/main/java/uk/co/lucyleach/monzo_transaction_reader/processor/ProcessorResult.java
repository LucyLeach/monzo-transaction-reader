package uk.co.lucyleach.monzo_transaction_reader.processor;

import uk.co.lucyleach.monzo_transaction_reader.monzo_model.Transaction;
import uk.co.lucyleach.monzo_transaction_reader.output_model.ProcessedTransaction;

import javax.annotation.Nullable;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: Lucy
 * Date: 10/08/2018
 * Time: 15:51
 */
public class ProcessorResult {
  private final Transaction originalTransaction;
  @Nullable
  private final Set<ProcessedTransaction> processedTransactions;
  @Nullable
  private final Exception error;

  public ProcessorResult(Transaction originalTransaction, Set<ProcessedTransaction> processedTransactions) {
    checkNotNull(processedTransactions);
    this.originalTransaction = originalTransaction;
    this.processedTransactions = Set.copyOf(processedTransactions);
    this.error = null;
  }

  public ProcessorResult(Transaction originalTransaction, Exception error) {
    checkNotNull(error);
    this.originalTransaction = originalTransaction;
    this.processedTransactions = null;
    this.error = error;
  }

  public Transaction getOriginalTransaction() {
    return originalTransaction;
  }

  @Nullable
  public Set<ProcessedTransaction> getProcessedTransactions() {
    return processedTransactions;
  }

  @Nullable
  public Exception getError() {
    return error;
  }
}
