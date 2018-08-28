package uk.co.lucyleach.monzo_transaction_reader.processor;

import uk.co.lucyleach.monzo_transaction_reader.monzo_model.Transaction;

import java.util.Collection;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

/**
 * User: Lucy
 * Date: 29/07/2018
 * Time: 21:18
 */
public class TransactionProcessorResult {
  private final Collection<ProcessorResult> processorResults;

  public TransactionProcessorResult(Collection<ProcessorResult> results) {
    this.processorResults = Set.copyOf(results);
  }

  public Collection<SuccessfulProcessorResult> getSuccessfulResults() {
    return processorResults.stream()
        .filter(ProcessorResult::isProcessedResult)
        .map(res -> new SuccessfulProcessorResult(res.getOriginalTransaction(), res.getProcessedTransactions()))
        .collect(toSet());
  }

  public Collection<UnsuccessfulProcessorResult> getUnsuccessfulResults() {
    return processorResults.stream()
        .filter(ProcessorResult::isErrorResult)
        .map(res -> new UnsuccessfulProcessorResult(res.getOriginalTransaction(), res.getErrorMessage()))
        .collect(toSet());
  }

  public Collection<Transaction> getIgnoredTransactions() {
    return processorResults.stream()
        .filter(ProcessorResult::isIgnoredResult)
        .map(ProcessorResult::getOriginalTransaction)
        .collect(toSet());
  }
}
