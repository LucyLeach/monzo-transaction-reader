package uk.co.lucyleach.monzo_transaction_reader.processor;

import uk.co.lucyleach.monzo_transaction_reader.monzo_model.Transaction;
import uk.co.lucyleach.monzo_transaction_reader.monzo_model.TransactionList;
import uk.co.lucyleach.monzo_transaction_reader.output_model.Money;
import uk.co.lucyleach.monzo_transaction_reader.output_model.SaleTransaction;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static java.util.stream.Collectors.toSet;

/**
 * User: Lucy
 * Date: 05/08/2018
 * Time: 20:28
 */
public class TransactionProcessor {
  final static String POT_PREFIX = "pot_";
  private final TagParser tagParser = new TagParser();

  public TransactionProcessorResult process(TransactionList transactions, ClientProcessingDetails clientDetails) {
    var results = transactions.getTransactions().stream()
        .map(t -> process(t, clientDetails))
        .collect(toSet());
    return new TransactionProcessorResult(results, Set.of());
  }

  private ResultOrException<SuccessfulProcessorResult> process(Transaction original, ClientProcessingDetails clientDetails) {
    if(isSaleTransaction(original)) {
      return processSaleTransaction(original);
    } else {
      return ResultOrException.createException(new ParsingException("Unimplemented transaction type", original));
    }
  }

  private ResultOrException<SuccessfulProcessorResult> processSaleTransaction(Transaction original) {
    String notes = original.getNotes();
    int amount = original.getAmount();
    Map<String, Integer> tagsAndAmounts;
    try {
      tagsAndAmounts = tagParser.parseTags(notes, amount);
    } catch(ParsingException e) {
      return ResultOrException.createException(e.changeTransaction(original));
    }

    var processedTransactions = tagsAndAmounts.entrySet().stream()
        .map(createTransaction(original))
        .collect(toSet());
    return ResultOrException.createResult(new SuccessfulProcessorResult(original, processedTransactions));
  }

  private static boolean isSaleTransaction(Transaction original) {
    return original.getMerchant() != null;
  }

  private static boolean isPotTransaction(Transaction original) {
    return original.getDescription().startsWith(POT_PREFIX);
  }

  private static Function<Map.Entry<String, Integer>, SaleTransaction> createTransaction(Transaction original) {
    return entry -> new SaleTransaction(original.getId(), convertDateTime(original.getCreated()),
            new Money(entry.getValue(), original.getCurrency()), original.getMerchant().getName(), entry.getKey());
  }

  private static ZonedDateTime convertDateTime(String dateTimeString) {
    return Instant.parse(dateTimeString).atZone(ZoneId.of("UTC"));
  }
}
