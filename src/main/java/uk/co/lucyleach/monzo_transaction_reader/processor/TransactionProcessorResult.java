package uk.co.lucyleach.monzo_transaction_reader.processor;

import java.util.Collection;

/**
 * User: Lucy
 * Date: 29/07/2018
 * Time: 21:18
 */
public class TransactionProcessorResult
{
  private final Collection<SuccessfulProcessorResult> successfulResults;
  private final Collection<UnsuccessfulProcessorResult> unsuccessfulResults;

  public TransactionProcessorResult(Collection<SuccessfulProcessorResult> successfulResults, Collection<UnsuccessfulProcessorResult> unsuccessfulResults)
  {
    this.successfulResults = successfulResults;
    this.unsuccessfulResults = unsuccessfulResults;
  }

}
