package uk.co.lucyleach.monzo_transaction_reader.processor;

import java.util.Collection;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

/**
 * User: Lucy
 * Date: 29/07/2018
 * Time: 21:18
 */
//TODO after completing processor get rid of the split between success/fail here and consequently this entire class
public class TransactionProcessorResult {
  private final Set<ProcessorResult> results;

  public TransactionProcessorResult(Set<ProcessorResult> results) {
    this.results = Set.copyOf(results);
  }

  public Collection<SuccessfulProcessorResult> getSuccessfulResults() {
    return results.stream()
        .filter(r -> r.getProcessedTransactions() != null)
        .map(r -> new SuccessfulProcessorResult(r.getOriginalTransaction(), r.getProcessedTransactions()))
        .collect(toSet());
  }

  public Collection<UnsuccessfulProcessorResult> getUnsuccessfulResults() {
    return results.stream()
        .filter(r -> r.getError() != null)
        .map(r -> new UnsuccessfulProcessorResult(r.getOriginalTransaction(), r.getError().getMessage()))
        .collect(toSet());
  }
}
