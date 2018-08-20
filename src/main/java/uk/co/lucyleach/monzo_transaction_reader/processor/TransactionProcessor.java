package uk.co.lucyleach.monzo_transaction_reader.processor;

import uk.co.lucyleach.monzo_transaction_reader.monzo_model.Transaction;
import uk.co.lucyleach.monzo_transaction_reader.monzo_model.TransactionList;
import uk.co.lucyleach.monzo_transaction_reader.output_model.Money;
import uk.co.lucyleach.monzo_transaction_reader.output_model.SaleTransaction;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import static java.util.stream.Collectors.toSet;

/**
 * User: Lucy
 * Date: 05/08/2018
 * Time: 20:28
 */
public class TransactionProcessor {
  private final TagParser tagParser = new TagParser();

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
      return processSaleTransaction(original, failures);
    } else {
      failures.add(new UnsuccessfulProcessorResult(original, "Only implemented sale transactions"));
      return null;
    }
  }

  private SuccessfulProcessorResult processSaleTransaction(Transaction original, Set<UnsuccessfulProcessorResult> failures) {
    String notes = original.getNotes();
    int amount = original.getAmount();
    Map<String, Integer> tagsAndAmounts;
    try {
      tagsAndAmounts = tagParser.parseTags(notes, amount);
    } catch(TagParser.ParsingException e) {
      failures.add(new UnsuccessfulProcessorResult(original, e.getMessage()));
      return null;
    }

    var processedTransactions = tagsAndAmounts.entrySet().stream()
        .map(createTransaction(original))
        .collect(toSet());
    return new SuccessfulProcessorResult(original, processedTransactions);
  }

  private static Function<Map.Entry<String, Integer>, SaleTransaction> createTransaction(Transaction original) {
    return entry -> new SaleTransaction(original.getId(), convertDateTime(original.getCreated()),
            new Money(entry.getValue(), original.getCurrency()), original.getMerchant().getName(), entry.getKey());
  }

  private static ZonedDateTime convertDateTime(String dateTimeString) {
    return Instant.parse(dateTimeString).atZone(ZoneId.of("UTC"));
  }
}
