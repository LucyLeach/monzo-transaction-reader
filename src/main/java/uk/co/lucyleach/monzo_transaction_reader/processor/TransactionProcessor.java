package uk.co.lucyleach.monzo_transaction_reader.processor;

import uk.co.lucyleach.monzo_transaction_reader.monzo_model.Transaction;
import uk.co.lucyleach.monzo_transaction_reader.monzo_model.TransactionList;

import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

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
        .map(notImplementedResult())
        .collect(toList());
    return new TransactionProcessorResult(Set.of(), unimplementedTransactions);
  }

  private static Function<Transaction, UnsuccessfulProcessorResult> notImplementedResult() {
    return t -> new UnsuccessfulProcessorResult(t, "Not implemented yet");
  }

  private static Predicate<Transaction> isSaleTransaction() {
    return t -> t.getMerchant() != null;
  }
}
