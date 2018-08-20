package uk.co.lucyleach.monzo_transaction_reader.processor;

import uk.co.lucyleach.monzo_transaction_reader.monzo_model.Transaction;
import uk.co.lucyleach.monzo_transaction_reader.monzo_model.TransactionList;
import uk.co.lucyleach.monzo_transaction_reader.output_model.Money;
import uk.co.lucyleach.monzo_transaction_reader.output_model.SaleTransaction;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

/**
 * User: Lucy
 * Date: 05/08/2018
 * Time: 20:28
 */
public class TransactionProcessor {
  public TransactionProcessorResult process(TransactionList transactions) {
    var failures = new HashSet<UnsuccessfulProcessorResult>();
    var results = transactions.getTransactions().stream()
        .map(trans -> process(trans, failures))
        .filter(Objects::nonNull)
        .collect(toSet());
    return new TransactionProcessorResult(results, failures);
  }

  private SuccessfulProcessorResult process(Transaction original, Set<UnsuccessfulProcessorResult> failures) {
    if(original.getMerchant() != null) {
      var processedTransactions = Set.of(new SaleTransaction(original.getId(), convertDateTime(original.getCreated()),
          new Money(original.getAmount(), original.getCurrency()),
          original.getMerchant().getName(), original.getNotes())); //TODO incorrect tag parsing!
      return new SuccessfulProcessorResult(original, processedTransactions);
    } else {
      failures.add(new UnsuccessfulProcessorResult(original, "Only implemented sale transactions"));
      return null;
    }
  }

  private static ZonedDateTime convertDateTime(String dateTimeString) {
    return Instant.parse(dateTimeString).atZone(ZoneId.of("UTC"));
  }
}
