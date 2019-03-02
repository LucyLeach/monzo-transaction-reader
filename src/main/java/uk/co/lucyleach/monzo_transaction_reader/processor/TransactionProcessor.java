package uk.co.lucyleach.monzo_transaction_reader.processor;

import uk.co.lucyleach.monzo_transaction_reader.monzo_model.Counterparty;
import uk.co.lucyleach.monzo_transaction_reader.monzo_model.Transaction;
import uk.co.lucyleach.monzo_transaction_reader.monzo_model.TransactionList;
import uk.co.lucyleach.monzo_transaction_reader.output_model.Money;
import uk.co.lucyleach.monzo_transaction_reader.output_model.ProcessedTransaction;
import uk.co.lucyleach.monzo_transaction_reader.output_model.SaleTransaction;
import uk.co.lucyleach.monzo_transaction_reader.output_model.TransferTransaction;

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
  final static String IGNORE_TAG = "#ignore";
  final static String GBP = "GBP";
  private final TagParser tagParser = new TagParser();
  private final TagCleaner tagCleaner = new TagCleaner();

  public TransactionProcessorResult process(TransactionList transactions, ClientProcessingDetails clientDetails, Map<String, String> potIdMap) {
    var results = transactions.getTransactions().stream()
        .map(t -> process(t, clientDetails, potIdMap))
        .collect(toSet());
    return new TransactionProcessorResult(results);
  }

  private ProcessorResult process(Transaction original, ClientProcessingDetails clientDetails, Map<String, String> potIdMap) {
    if(original.getAmount() == 0) {
      return createIgnoredResult(original, ReasonIgnored.ZERO_TRANSACTION);
    } else if(!GBP.equals(original.getCurrency())) {
      return createIgnoredResult(original, ReasonIgnored.NON_GBP);
    } else if(original.isDeclined()) {
      return createIgnoredResult(original, ReasonIgnored.DECLINED);
    } else if(isSaleTransaction(original)) {
      return processSaleTransaction(original, clientDetails);
    } else if(isPotTransaction(original)) {
      return processPotTransaction(original, clientDetails, potIdMap);
    } else if(isTransferTransaction(original)) {
      return processTransferTransaction(original, clientDetails);
    } else {
      return createErrorResult(original, "Unimplemented transaction type");
    }
  }

  private ProcessorResult processSaleTransaction(Transaction original, ClientProcessingDetails clientDetails) {
    return processTaggedTransaction(original, saleTransactionNoteGetter(clientDetails), t -> createSaleTransactionWithTag(t, clientDetails));
  }

  private ProcessorResult processTransferTransaction(Transaction original, ClientProcessingDetails clientDetails) {
    return processTaggedTransaction(original, transferTransactionNoteGetter(clientDetails), t -> createTransferTransactionWithTag(t, clientDetails));
  }

  private ProcessorResult processTaggedTransaction(Transaction original, Function<Transaction, String> noteGetter,
                                                   Function<Transaction, Function<Map.Entry<String, Integer>, ProcessedTransaction>> tagToTransactionFunction) {
    var notes = noteGetter.apply(original);
    if(checkForIgnoreTag(notes)) {
      return ProcessorResult.createIgnoredResult(original, ReasonIgnored.IGNORE_TAG);
    }

    var amount = original.getAmount();
    Map<String, Integer> tagsAndAmounts;
    try {
      tagsAndAmounts = tagParser.parseTags(notes, amount);
    } catch(ParsingException e) {
      return createErrorResult(original, e.getMessage());
    }

    var processedTransactions = tagsAndAmounts.entrySet().stream()
        .map(tagToTransactionFunction.apply(original))
        .collect(toSet());
    return createProcessedResult(original, processedTransactions);
  }

  private ProcessorResult processPotTransaction(Transaction original, ClientProcessingDetails clientDetails, Map<String, String> potIdMap) {
    var potId = original.getDescription();
    var potName = potIdMap.getOrDefault(potId, potId);
    var isInTransaction = original.getAmount() > 0;
    var inDetails = isInTransaction ? clientDetails.getPotsToRecogniseIn().containsKey(potId) : clientDetails.getPotsToRecogniseOut().containsKey(potId);
    if(!inDetails) {
      return createIgnoredResult(original, ReasonIgnored.UNCONFIGURED_POT, potName);
    } else if(checkForIgnoreTag(original.getNotes())) {
      return createIgnoredResult(original, ReasonIgnored.IGNORE_TAG);
    } else {
      var hashTag = isInTransaction ? clientDetails.getPotsToRecogniseIn().get(potId) : clientDetails.getPotsToRecogniseOut().get(potId);
      if(checkForIgnoreTag(hashTag)) {
        return createIgnoredResult(original, ReasonIgnored.IGNORE_TAG);
      }
      var tag = hashTag.replace("#", "");
      var processedTransaction = new TransferTransaction(original.getId(), convertDateTime(original.getCreated()), new Money(original.getAmount(), original.getCurrency()),
          potName, tag);
      return createProcessedResult(original, Set.of(processedTransaction));
    }
  }

  private static boolean checkForIgnoreTag(String hashTag) {
    return hashTag.toLowerCase().contains(IGNORE_TAG);
  }

  private static boolean isSaleTransaction(Transaction original) {
    return original.getMerchant() != null && original.getMerchant().getName() != null;
  }

  private static boolean isPotTransaction(Transaction original) {
    return original.getDescription().startsWith(POT_PREFIX);
  }

  private static boolean isTransferTransaction(Transaction original) {
    return original.getCounterparty() != null && original.getCounterparty().isNonEmpty();
  }

  private static Function<Transaction, String> saleTransactionNoteGetter(ClientProcessingDetails clientDetails) {
    //NB Does not override existing notes
    return transaction -> {
      if(clientDetails.getAutoTagMerchants().containsKey(transaction.getMerchant().getName())) {
        return clientDetails.getAutoTagMerchants().get(transaction.getMerchant().getName());
      } else {
        return transaction.getNotes();
      }
    };
  }

  private static Function<Transaction, String> transferTransactionNoteGetter(ClientProcessingDetails clientDetails) {
    return transaction -> {
      var accountId = transaction.getCounterparty().getAccountId();
      //NB overrides existing notes unless ignored
      if(clientDetails.getAutoTagAccounts().containsKey(accountId) && !checkForIgnoreTag(transaction.getNotes())) {
        return clientDetails.getAutoTagAccounts().get(accountId);
      } else {
        return transaction.getNotes();
      }
    };
  }

  private Function<Map.Entry<String, Integer>, ProcessedTransaction> createSaleTransactionWithTag(Transaction original, ClientProcessingDetails clientDetails) {
    return entry -> new SaleTransaction(original.getId(), convertDateTime(original.getCreated()),
        new Money(entry.getValue(), original.getCurrency()), original.getMerchant().getName(), tagCleaner.cleanTag(entry.getKey(), clientDetails));
  }

  private Function<Map.Entry<String, Integer>, ProcessedTransaction> createTransferTransactionWithTag(Transaction original, ClientProcessingDetails clientDetails) {
    return entry -> new TransferTransaction(original.getId(), convertDateTime(original.getCreated()), new Money(entry.getValue(), original.getCurrency()),
        createWhereFromString(original.getDescription(), original.getCounterparty()), tagCleaner.cleanTag(entry.getKey(), clientDetails));
  }

  private static ZonedDateTime convertDateTime(String dateTimeString) {
    return Instant.parse(dateTimeString).atZone(ZoneId.of("UTC"));
  }

  private static String createWhereFromString(String description, Counterparty counterparty) {
    return counterparty.getAccountId() + " - " + description;
  }
}
