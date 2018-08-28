package uk.co.lucyleach.monzo_transaction_reader.processor;

import uk.co.lucyleach.monzo_transaction_reader.monzo_model.Transaction;

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
  private final Collection<Transaction> ignoredTransactions;

  public TransactionProcessorResult(Collection<ResultOrException<SuccessfulProcessorResult>> resultsOrExceptions, Collection<Transaction> ignoredTransactions) {
    this.resultsOrExceptions = Set.copyOf(resultsOrExceptions);
    this.ignoredTransactions = Set.copyOf(ignoredTransactions);
  }

  public Collection<SuccessfulProcessorResult> getSuccessfulResults() {
    return resultsOrExceptions.stream()
        .filter(ResultOrException::isSuccess)
        .map(ResultOrException::getResult)
        .collect(Collectors.toSet());
  }

  public Collection<UnsuccessfulProcessorResult> getUnsuccessfulResults() {
    return resultsOrExceptions.stream()
        .filter(ResultOrException::isException)
        .map(ResultOrException::getException)
        .map(e -> new UnsuccessfulProcessorResult(e.getTransaction(), e.getMessage()))
        .collect(Collectors.toSet());
  }

  public Collection<Transaction> getIgnoredTransactions() {
    return ignoredTransactions;
  }
}
