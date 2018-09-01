package uk.co.lucyleach.monzo_transaction_reader.processor;

import uk.co.lucyleach.monzo_transaction_reader.monzo_model.Transaction;
import uk.co.lucyleach.monzo_transaction_reader.monzo_model.TransactionList;
import uk.co.lucyleach.monzo_transaction_reader.output_model.Money;
import uk.co.lucyleach.monzo_transaction_reader.output_model.SaleTransaction;
import uk.co.lucyleach.monzo_transaction_reader.output_model.TransferIn;
import uk.co.lucyleach.monzo_transaction_reader.output_model.TransferOut;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static java.util.stream.Collectors.toSet;
import static uk.co.lucyleach.monzo_transaction_reader.processor.ProcessorResult.*;

/**
 * User: Lucy
 * Date: 05/08/2018
 * Time: 20:28
 */
public class TransactionProcessor {
  final static String POT_PREFIX = "pot_";
  final static String IGNORE_TAG = "#Ignore";
  private final TagParser tagParser = new TagParser();

  public TransactionProcessorResult process(TransactionList transactions, ClientProcessingDetails clientDetails) {
    var results = transactions.getTransactions().stream()
        .map(t -> process(t, clientDetails))
        .collect(toSet());
    return new TransactionProcessorResult(results);
  }

  private ProcessorResult process(Transaction original, ClientProcessingDetails clientDetails) {
    if(original.getAmount() == 0 || original.getNotes().contains(IGNORE_TAG)) {
      return createIgnoredResult(original);
    } else if(isSaleTransaction(original)) {
      return processSaleTransaction(original);
    } else if(isPotTransaction(original)) {
      return processPotTransaction(original, clientDetails);
    } else {
      return createErrorResult(original, "Unimplemented transaction type");
    }
  }

  private ProcessorResult processSaleTransaction(Transaction original) {
    var notes = original.getNotes();
    var amount = original.getAmount();
    Map<String, Integer> tagsAndAmounts;
    try {
      tagsAndAmounts = tagParser.parseTags(notes, amount);
    } catch(ParsingException e) {
      return createErrorResult(original, e.getMessage());
    }

    var processedTransactions = tagsAndAmounts.entrySet().stream()
        .map(createTransaction(original))
        .collect(toSet());
    return createProcessedResult(original, processedTransactions);
  }

  private ProcessorResult processPotTransaction(Transaction original, ClientProcessingDetails clientDetails) {
    var potId = original.getDescription();
    var isInTransaction = original.getAmount() > 0;
    var inDetails = isInTransaction ? clientDetails.getPotsToRecogniseIn().containsKey(potId) : clientDetails.getPotsToRecogniseOut().containsKey(potId);
    var toIgnore = !inDetails || original.getAmount() == 0;
    if(toIgnore) {
      return createIgnoredResult(original);
    } else if(isInTransaction) {
      var tag = clientDetails.getPotsToRecogniseIn().get(potId);
      var processedTransaction = new TransferIn(original.getId(), convertDateTime(original.getCreated()), new Money(original.getAmount(), original.getCurrency()),
          original.getDescription(), false, tag);
      return createProcessedResult(original, Set.of(processedTransaction));
    } else {
      var tag = clientDetails.getPotsToRecogniseOut().get(potId);
      var processedTransaction = new TransferOut(original.getId(), convertDateTime(original.getCreated()), new Money(original.getAmount(), original.getCurrency()),
          original.getDescription(), tag);
      return createProcessedResult(original, Set.of(processedTransaction));
    }
  }

  private static boolean isSaleTransaction(Transaction original) {
    return original.getMerchant() != null;
  }

  private static boolean isPotTransaction(Transaction original) {
    return original.getDescription().startsWith(POT_PREFIX);
  }

  public static boolean isTransfer(Transaction original) {
    return original.getCounterparty() != null && original.getCounterparty().isNonEmpty();
  }

  private static Function<Map.Entry<String, Integer>, SaleTransaction> createTransaction(Transaction original) {
    return entry -> new SaleTransaction(original.getId(), convertDateTime(original.getCreated()),
            new Money(entry.getValue(), original.getCurrency()), original.getMerchant().getName(), entry.getKey());
  }

  private static ZonedDateTime convertDateTime(String dateTimeString) {
    return Instant.parse(dateTimeString).atZone(ZoneId.of("UTC"));
  }
}
