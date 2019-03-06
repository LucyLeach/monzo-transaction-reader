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
  private final Set<ProcessedTransaction> processedTransactions;
  @Nullable
  private final String errorMessage;
  @Nullable
  private final ReasonIgnored reasonIgnored;

  private ProcessorResult(Transaction originalTransaction, @Nullable Set<ProcessedTransaction> processedTransactions, @Nullable String errorMessage, @Nullable ReasonIgnored reasonIgnored) {
    this.originalTransaction = originalTransaction;
    this.processedTransactions = processedTransactions != null ? Set.copyOf(processedTransactions) : null;
    this.errorMessage = errorMessage;
    this.reasonIgnored = reasonIgnored;
  }

  public boolean isProcessedResult() {
    return processedTransactions != null;
  }

  public boolean isErrorResult() {
    return errorMessage != null;
  }

  public boolean isIgnoredResult() {
    return reasonIgnored != null;
  }

  public Transaction getOriginalTransaction() {
    return originalTransaction;
  }

  @Nullable
  public Set<ProcessedTransaction> getProcessedTransactions() {
    return processedTransactions;
  }

  @Nullable
  public String getErrorMessage() {
    return errorMessage;
  }

  @Nullable
  public ReasonIgnored getReasonIgnored() {
    return reasonIgnored;
  }

  public static ProcessorResult createProcessedResult(Transaction originalTransaction, Set<ProcessedTransaction> processedTransactions) {
    checkNotNull(originalTransaction, processedTransactions);
    return new ProcessorResult(originalTransaction, processedTransactions, null, null);
  }

  public static ProcessorResult createErrorResult(Transaction originalTransaction, String errorMessage) {
    checkNotNull(originalTransaction, errorMessage);
    return new ProcessorResult(originalTransaction, null, errorMessage, null);
  }

  public static ProcessorResult createIgnoredResult(Transaction originalTransaction, SimpleReasonIgnored reasonIgnored) {
    checkNotNull(originalTransaction, reasonIgnored);
    return new ProcessorResult(originalTransaction, null, null, reasonIgnored);
  }

  public static ProcessorResult createIgnoredPotResult(Transaction originalTransaction, String potName) {
    checkNotNull(originalTransaction, potName);
    return new ProcessorResult(originalTransaction, null, null, new ReasonIgnored_Pot(potName));
  }
}
