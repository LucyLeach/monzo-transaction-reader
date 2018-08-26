package uk.co.lucyleach.monzo_transaction_reader.processor;

import uk.co.lucyleach.monzo_transaction_reader.monzo_model.Transaction;
import uk.co.lucyleach.monzo_transaction_reader.monzo_model.TransactionList;
import uk.co.lucyleach.monzo_transaction_reader.output_model.Money;
import uk.co.lucyleach.monzo_transaction_reader.output_model.SaleTransaction;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toSet;

/**
 * User: Lucy
 * Date: 05/08/2018
 * Time: 20:28
 */
public class TransactionProcessor {
  private final TagParser tagParser = new TagParser();

  public TransactionProcessorResult process(TransactionList transactions) {
    var failures = new HashMap<Transaction, String>();
    var results = transactions.getTransactions().stream()
        .filter(elseException(isSale(), exceptionConsumer(failures, "Only implemented sale transactions")))
        .map(trans -> processSaleTransaction(trans, failures))
        .filter(Objects::nonNull)
        .collect(toSet());
    var unsuccessfulResults = failures.entrySet().stream().map(e -> new UnsuccessfulProcessorResult(e.getKey(), e.getValue())).collect(toSet());
    return new TransactionProcessorResult(results, unsuccessfulResults);
  }

  private static Predicate<Transaction> isSale() {
    return t -> t.getMerchant() != null;
  }

  private <T> Predicate<T> elseException(Predicate<T> filter, Consumer<T> consumer) {
    return t -> {
      if(filter.test(t)) {
        return true;
      } else {
        consumer.accept(t);
        return false;
      }
    };
  }

  private static <T> Consumer<T> exceptionConsumer(Map<T, String> exceptions, String exceptionMessage) {
    return t -> exceptions.put(t, exceptionMessage);
  }

  private SuccessfulProcessorResult processSaleTransaction(Transaction original, Map<Transaction, String> failures) {
    String notes = original.getNotes();
    int amount = original.getAmount();
    Map<String, Integer> tagsAndAmounts;
    try {
      tagsAndAmounts = tagParser.parseTags(notes, amount);
    } catch(TagParser.ParsingException e) {
      failures.put(original, e.getMessage());
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
