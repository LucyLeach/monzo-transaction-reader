package uk.co.lucyleach.monzo_transaction_reader.processor;

import java.util.Collection;
import java.util.Set;

/**
 * User: Lucy
 * Date: 29/07/2018
 * Time: 21:18
 */
public class TransactionProcessorResult {
  private final Collection<SuccessfulProcessorResult> successfulResults;
  private final Collection<UnsuccessfulProcessorResult> unsuccessfulResults;

  public TransactionProcessorResult(Collection<SuccessfulProcessorResult> successfulResults, Collection<UnsuccessfulProcessorResult> unsuccessfulResults) {
    this.successfulResults = Set.copyOf(successfulResults);
    this.unsuccessfulResults = Set.copyOf(unsuccessfulResults);
  }

  public Collection<SuccessfulProcessorResult> getSuccessfulResults() {
    return successfulResults;
  }

  public Collection<UnsuccessfulProcessorResult> getUnsuccessfulResults() {
    return unsuccessfulResults;
  }
}
