package uk.co.lucyleach.monzo_transaction_reader.processor;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * User: Lucy
 * Date: 29/07/2018
 * Time: 21:18
 */
public class TransactionProcessorResult {
  private final Collection<ResultOrException<SuccessfulProcessorResult>> resultsOrExceptions;

  public TransactionProcessorResult(Collection<ResultOrException<SuccessfulProcessorResult>> resultsOrExceptions) {
    this.resultsOrExceptions = Set.copyOf(resultsOrExceptions);
  }

  public Collection<SuccessfulProcessorResult> getSuccessfulResults() {
    return resultsOrExceptions.stream()
        .filter(ResultOrException::isSuccess)
        .map(ResultOrException::getResult)
        .collect(Collectors.toSet());
  }

  public Collection<UnsuccessfulProcessorResult> getUnsuccessfulResults() {
    return resultsOrExceptions.stream()
        .filter(roe -> !roe.isSuccess())
        .map(ResultOrException::getException)
        .map(e -> new UnsuccessfulProcessorResult(e.getTransaction(), e.getMessage()))
        .collect(Collectors.toSet());
  }
}
