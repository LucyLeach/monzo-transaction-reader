package uk.co.lucyleach.monzo_transaction_reader.processor;

import uk.co.lucyleach.monzo_transaction_reader.monzo_model.Transaction;
import uk.co.lucyleach.monzo_transaction_reader.output_model.ProcessedTransaction;

import javax.annotation.Nullable;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: Lucy
 * Date: 28/08/2018
 * Time: 20:15
 */
public class ProcessorResult {
  private final Transaction originalTransaction;
  @Nullable
  private final Set<? extends ProcessedTransaction> processedTransactions;
  @Nullable
  private final String errorMessage;

  private ProcessorResult(Transaction originalTransaction, @Nullable Set<? extends ProcessedTransaction> processedTransactions, @Nullable String errorMessage) {
    this.originalTransaction = originalTransaction;
    this.processedTransactions = processedTransactions != null ? Set.copyOf(processedTransactions) : null;
    this.errorMessage = errorMessage;
  }

  public static ProcessorResult createProcessedResult(Transaction originalTransaction, Set<? extends ProcessedTransaction> processedTransactions) {
    checkNotNull(originalTransaction, processedTransactions);
    return new ProcessorResult(originalTransaction, processedTransactions, null);
  }

  public static ProcessorResult createErrorResult(Transaction originalTransaction, String errorMessage) {
    checkNotNull(originalTransaction, errorMessage);
    return new ProcessorResult(originalTransaction, null, errorMessage);
  }

  public static ProcessorResult createIgnoredResult(Transaction originalTransaction) {
    checkNotNull(originalTransaction);
    return new ProcessorResult(originalTransaction, null, null);
  }

  public boolean isProcessedResult() {
    return processedTransactions != null;
  }

  public boolean isErrorResult() {
    return errorMessage != null;
  }

  public boolean isIgnoredResult() {
    return processedTransactions == null && errorMessage == null;
  }

  public Transaction getOriginalTransaction() {
    return originalTransaction;
  }

  @Nullable
  public Set<? extends ProcessedTransaction> getProcessedTransactions() {
    return processedTransactions;
  }

  @Nullable
  public String getErrorMessage() {
    return errorMessage;
  }
}
