package uk.co.lucyleach.monzo_transaction_reader.processor;

import uk.co.lucyleach.monzo_transaction_reader.monzo_model.Transaction;
import uk.co.lucyleach.monzo_transaction_reader.output_model.ProcessedTransaction;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toMap;
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

  public Map<Transaction, Set<ProcessedTransaction>> getSuccessfulResults() {
    return processorResults.stream()
        .filter(ProcessorResult::isProcessedResult)
        .collect(toMap(ProcessorResult::getOriginalTransaction, ProcessorResult::getProcessedTransactions));
  }

  public Map<Transaction, String> getUnsuccessfulResults() {
    return processorResults.stream()
        .filter(ProcessorResult::isErrorResult)
        .collect(toMap(ProcessorResult::getOriginalTransaction, ProcessorResult::getErrorMessage));
  }

  public Collection<Transaction> getIgnoredTransactions() {
    return processorResults.stream()
        .filter(ProcessorResult::isIgnoredResult)
        .map(ProcessorResult::getOriginalTransaction)
        .collect(toSet());
  }
}
