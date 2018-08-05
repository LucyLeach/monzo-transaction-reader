package uk.co.lucyleach.monzo_transaction_reader.processor;

import uk.co.lucyleach.monzo_transaction_reader.monzo_model.Transaction;
import uk.co.lucyleach.monzo_transaction_reader.monzo_model.TransactionList;

import java.util.Set;

import static java.util.stream.Collectors.toList;

/**
 * User: Lucy
 * Date: 05/08/2018
 * Time: 20:28
 */
public class TransactionProcessor
{
  public TransactionProcessorResult process(TransactionList transactions) {
    var unimplementedTransactions = transactions.getTransactions().stream()
        .map(TransactionProcessor::notImplementedResult)
        .collect(toList());
    return new TransactionProcessorResult(Set.of(), unimplementedTransactions);
  }

  private static UnsuccessfulProcessorResult notImplementedResult(Transaction transaction) {
    return new UnsuccessfulProcessorResult(transaction, "Not implemented yet");
  }
}
