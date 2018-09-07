package uk.co.lucyleach.monzo_transaction_reader.processor;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.junit.Ignore;
import org.junit.Test;
import uk.co.lucyleach.monzo_transaction_reader.monzo_model.Counterparty;
import uk.co.lucyleach.monzo_transaction_reader.monzo_model.Merchant;
import uk.co.lucyleach.monzo_transaction_reader.monzo_model.Transaction;
import uk.co.lucyleach.monzo_transaction_reader.monzo_model.TransactionList;
import uk.co.lucyleach.monzo_transaction_reader.output_model.Money;
import uk.co.lucyleach.monzo_transaction_reader.output_model.ProcessedTransaction;
import uk.co.lucyleach.monzo_transaction_reader.output_model.SaleTransaction;
import uk.co.lucyleach.monzo_transaction_reader.output_model.TransferTransaction;
import uk.co.lucyleach.monzo_transaction_reader.utils.Pair;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.*;
import static org.junit.Assert.*;
import static uk.co.lucyleach.monzo_transaction_reader.processor.TransactionProcessor.IGNORE_TAG;
import static uk.co.lucyleach.monzo_transaction_reader.processor.TransactionProcessor.POT_PREFIX;

/**
 * User: Lucy
 * Date: 05/08/2018
 * Time: 20:37
 */
@Ignore
public class TransactionProcessorTest {
  private static final TransactionProcessor UNDER_TEST = new TransactionProcessor();

  @Test
  public void testEmptyInput() {
    var result = UNDER_TEST.process(new TransactionList(), ClientProcessingDetails.builder().create());
    checkForNulls(result);
    checkNoSuccessfulResults(result);
    checkNoUnsuccessfulResults(result);
    checkNoIgnoredTransactions(result);
  }

  @Test
  public void testSingleSaleTransaction() {
    var expectedResult = createSingleSaleTransaction();

    var result = UNDER_TEST.process(new TransactionList(expectedResult.getOriginalTransaction()), ClientProcessingDetails.builder().create());

    checkForNulls(result);
    checkSuccessfulResult(result.getSuccessfulResults(), expectedResult);
    checkNoUnsuccessfulResults(result);
    checkNoIgnoredTransactions(result);
  }

  @Test
  public void testMultipleTransactions() {
    var expectedResult1 = createSingleSaleTransaction(1);
    var expectedResult2 = createSingleSaleTransaction(2);

    var result = UNDER_TEST.process(new TransactionList(expectedResult1.getA(), expectedResult2.getA()), ClientProcessingDetails.builder().create());

    checkForNulls(result);
    checkSuccessfulResult(result.getSuccessfulResults(), expectedResult1, expectedResult2);
    checkNoUnsuccessfulResults(result);
    checkNoIgnoredTransactions(result);
  }

  @Test
  public void testMultiTagTransaction() {
    var tagsToAmounts = Map.of("Tag1", 350, "Tag2", 600, "Tag3", 210);
    var expectedSuccessfulResult = createSaleTransactions(1, tagsToAmounts);

    var result = UNDER_TEST.process(new TransactionList(expectedSuccessfulResult.getOriginalTransaction()), ClientProcessingDetails.builder().create());

    checkForNulls(result);
    checkSuccessfulResult(result.getSuccessfulResults(), expectedSuccessfulResult);
    checkNoUnsuccessfulResults(result);
    checkNoIgnoredTransactions(result);
  }

  @Test
  public void testMultiTagIncludingRest() {
    var tagsToAmounts = Map.of("Tag1", 378, "Tag2", 890);
    var expectedSuccessfulResult = createSaleTransactions(1, tagsToAmounts, Optional.of(310));

    var result = UNDER_TEST.process(new TransactionList(expectedSuccessfulResult.getOriginalTransaction()), ClientProcessingDetails.builder().create());

    checkForNulls(result);
    checkSuccessfulResult(result.getSuccessfulResults(), expectedSuccessfulResult);
    checkNoUnsuccessfulResults(result);
    checkNoIgnoredTransactions(result);
  }

  @Test
  public void testLegacyNoHash() {
    var tagsToAmounts = Map.of("Tag1", 378, "Tag2", 890);
    var expectedSuccessfulResult = createSaleTransactions(1, tagsToAmounts, Optional.of(310));
    var originalTransaction = expectedSuccessfulResult.getOriginalTransaction();
    var notesWithoutHashes = originalTransaction.getNotes().replace("#", "");
    var inputWithoutHashes = new Transaction(originalTransaction.getId(), originalTransaction.getAmount(), originalTransaction.getCurrency(),
        originalTransaction.getCreated(), notesWithoutHashes, originalTransaction.getMerchant(), originalTransaction.getDescription(), emptyCounterparty());
    var expectedSuccessfulResultWoHashes = new InputAndOutputTransactions(inputWithoutHashes, expectedSuccessfulResult.getProcessedTransactions());

    var result = UNDER_TEST.process(new TransactionList(inputWithoutHashes), ClientProcessingDetails.builder().create());

    checkForNulls(result);
    checkSuccessfulResult(result.getSuccessfulResults(), expectedSuccessfulResultWoHashes);
    checkNoUnsuccessfulResults(result);
    checkNoIgnoredTransactions(result);
  }

  @Test
  public void testImpossibleTagsFail() {
    var inputSet = Set.of(
        createSimpleSaleTransaction("More than one tag, no semi colon separator", "#FirstTag #WhoopsSecondTag", -500),
        createSimpleSaleTransaction("More than one amount", "1 2", -500),
        createSimpleSaleTransaction("Separated tags but no amount", "#FirstTag; #SecondTag", -500),
        createSimpleSaleTransaction("Empty notes", "", -500), //NB will implement filters for some special Merchants at some point
        createSimpleSaleTransaction("Just text notes", "foo bar", -500),
        createSimpleSaleTransaction("Comma separated notes", "100 #FirstTag, 400 #SecondTag", -500),
        createSimpleSaleTransaction("Amounts don't add up", "100 #FirstTag; 100 #SecondTag", -250)
    );

    var result = UNDER_TEST.process(new TransactionList(Lists.newArrayList(inputSet)), ClientProcessingDetails.builder().create());

    checkForNulls(result);
    checkNoSuccessfulResults(result);
    checkForUnsuccessfulResults(inputSet, result);
    checkNoIgnoredTransactions(result);
  }

  @Test
  public void testSaleTransactionDefaultTag() {
    var merchantName = "Merchant Name";
    var defaultTag = "DefaultTag";
    var saleTransaction = new Transaction("No tag", -100, "GBP", "2018-02-01T08:00:00.0Z", "", new Merchant(merchantName), "Description", emptyCounterparty());
    var expectedOutputTransaction = new SaleTransaction("No tag", ZonedDateTime.of(2018, 2, 1, 8, 0, 0, 0, ZoneId.of("UTC")),
        new Money(-100, "GBP"), merchantName, defaultTag);
    var clientDetails = ClientProcessingDetails.builder()
        .addAutoTagMerchant(merchantName, "#" + defaultTag)
        .create();

    var result = UNDER_TEST.process(new TransactionList(saleTransaction), clientDetails);

    checkForNulls(result);
    checkSuccessfulResult(result.getSuccessfulResults(), new InputAndOutputTransactions(saleTransaction, expectedOutputTransaction));
    checkNoUnsuccessfulResults(result);
    checkNoIgnoredTransactions(result);
  }

  @Test
  public void testSaleTransactionCantOverrideTagWithDefault() {
    var merchantName = "Merchant Name";
    var defaultTag = "DefaultTag";
    var saleTransaction = new Transaction("Can't override tag", -100, "GBP", "2018-02-01T08:00:00.0Z", "#ExistingTag", new Merchant(merchantName), "Description", emptyCounterparty());
    var expectedOutputTransaction = new SaleTransaction("Can't override tag", ZonedDateTime.of(2018, 2, 1, 8, 0, 0, 0, ZoneId.of("UTC")),
        new Money(-100, "GBP"), merchantName, "ExistingTag");
    var clientDetails = ClientProcessingDetails.builder()
        .addAutoTagMerchant(merchantName, "#" + defaultTag)
        .create();

    var result = UNDER_TEST.process(new TransactionList(saleTransaction), clientDetails);

    checkForNulls(result);
    checkSuccessfulResult(result.getSuccessfulResults(), new InputAndOutputTransactions(saleTransaction, expectedOutputTransaction));
    checkNoUnsuccessfulResults(result);
    checkNoIgnoredTransactions(result);
  }

  @Test
  public void testUnrecognisedPot() {
    var potId = POT_PREFIX + "Unrecognized";
    var potTransferIn = createPotTransaction(potId, "tag", true).getOriginalTransaction();
    var potTransferOut = createPotTransaction(potId, "tag", false).getOriginalTransaction();

    var result = UNDER_TEST.process(new TransactionList(potTransferIn, potTransferOut), ClientProcessingDetails.builder().create());

    checkForNulls(result);
    checkNoSuccessfulResults(result);
    checkNoUnsuccessfulResults(result);
    checkIgnoredTransactions(result, potTransferIn, potTransferOut);
  }

  @Test
  public void testPotRecognisedInNotOut() {
    var potId = POT_PREFIX + "RecognizedInNotOut";
    var potTransferInResult = createPotTransaction(potId, "MappedTag", true);
    var potTransferOut = createPotTransaction(potId, "tag", false).getOriginalTransaction();

    var clientDetails = ClientProcessingDetails.builder().addPotsToRecogniseIn(Map.of(potId, "MappedTag")).create();

    var result = UNDER_TEST.process(new TransactionList(potTransferInResult.getOriginalTransaction(), potTransferOut), clientDetails);

    checkForNulls(result);
    checkSuccessfulResult(result.getSuccessfulResults(), potTransferInResult);
    checkNoUnsuccessfulResults(result);
    checkIgnoredTransactions(result, potTransferOut);
  }

  @Test
  public void testPotRecognisedOutNotIn() {
    var potId = POT_PREFIX + "RecognisedOutNotIn";
    var potTransferIn = createPotTransaction(potId, "tag", true).getOriginalTransaction();
    var potTransferOutResult = createPotTransaction(potId, "MappedTag", false);

    var clientDetails = ClientProcessingDetails.builder().addPotsToRecogniseOut(Map.of(potId, "MappedTag")).create();

    var result = UNDER_TEST.process(new TransactionList(potTransferOutResult.getOriginalTransaction(), potTransferIn), clientDetails);

    checkForNulls(result);
    checkSuccessfulResult(result.getSuccessfulResults(), potTransferOutResult);
    checkNoUnsuccessfulResults(result);
    checkIgnoredTransactions(result, potTransferIn);
  }

  @Test
  public void testPotRecognisedBothWays() {
    var potId = POT_PREFIX + "RecognisedBothWays";
    var potTransferInResult = createPotTransaction(potId, "MappedTagIn", true);
    var potTransferOutResult = createPotTransaction(potId, "MappedTagOut", false);

    var clientDetails = ClientProcessingDetails.builder().addPotsToRecogniseIn(Map.of(potId, "MappedTagIn")).addPotsToRecogniseOut(Map.of(potId, "MappedTagOut")).create();

    var result = UNDER_TEST.process(new TransactionList(potTransferOutResult.getOriginalTransaction(), potTransferInResult.getOriginalTransaction()), clientDetails);

    checkForNulls(result);
    checkSuccessfulResult(result.getSuccessfulResults(), potTransferOutResult, potTransferOutResult);
    checkNoUnsuccessfulResults(result);
    checkNoIgnoredTransactions(result);
  }

  @Test
  public void testIgnoreZeroTransactionOfAnyType() {
    var saleTransaction = createSimpleSaleTransaction("Zero transaction", "#foo", 0);

    var potId = POT_PREFIX + "ZeroAmount";
    var potTransferInResult = createPotTransaction(potId, "MappedTagIn", true, 0);
    var potTransferOutResult = createPotTransaction(potId, "MappedTagOut", false, 0);

    var clientDetails = ClientProcessingDetails.builder().addPotsToRecogniseIn(Map.of(potId, "MappedTagIn")).addPotsToRecogniseOut(Map.of(potId, "MappedTagOut")).create();

    var result = UNDER_TEST.process(new TransactionList(saleTransaction, potTransferOutResult.getOriginalTransaction(), potTransferInResult.getOriginalTransaction()), clientDetails);

    checkForNulls(result);
    checkNoSuccessfulResults(result);
    checkNoUnsuccessfulResults(result);
    checkIgnoredTransactions(result, saleTransaction, potTransferInResult.getOriginalTransaction(), potTransferOutResult.getOriginalTransaction());
  }

  @Test
  public void testIgnoreTag() {
    var justIgnoreTag = createSimpleSaleTransaction("Just ignore tag", IGNORE_TAG, -2930);
    var ignoreTagMiddle = createSimpleSaleTransaction("Ignore tag middle", "2.3 #groceries; 4.1 " + IGNORE_TAG + "; rest #toys", -1089);

    var potId = POT_PREFIX + "Ignore Tag";
    var dateString = "2018-01-07T08:00:00.0Z";
    var ignorePotInTransaction = new Transaction(potId + " in", 5631, "GBP", dateString, IGNORE_TAG, null, potId, emptyCounterparty());
    var ignorePotOutTransaction = new Transaction(potId + " out", -5631, "GBP", dateString, IGNORE_TAG, null, potId, emptyCounterparty());
    var ignoreTransferIn = new Transaction("Transfer in with ignore", 7312, "GBP", dateString, IGNORE_TAG, null, "Description", new Counterparty(123, 456));
    var ignoreTransferOut = new Transaction("Transfer out with ignore", -7312, "GBP", dateString, IGNORE_TAG, null, "Description", new Counterparty(123, 456));
    var clientDetails = ClientProcessingDetails.builder().addPotsToRecogniseIn(Map.of(potId, "MappedTagIn")).addPotsToRecogniseOut(Map.of(potId, "MappedTagOut")).create();

    var result = UNDER_TEST.process(new TransactionList(justIgnoreTag, ignoreTagMiddle, ignorePotInTransaction, ignorePotOutTransaction, ignoreTransferIn, ignoreTransferOut), clientDetails);

    checkForNulls(result);
    checkNoSuccessfulResults(result);
    checkNoUnsuccessfulResults(result);
    checkIgnoredTransactions(result, justIgnoreTag, ignoreTagMiddle, ignorePotInTransaction, ignorePotOutTransaction, ignoreTransferIn, ignoreTransferOut);
  }

  @Test
  public void testTransfersUnreadableTag() {
    var dateString = "2018-01-10T10:00:00.0Z";
    var transferIn = new Transaction("Unreadable tag - transfer in", 4320, "GBP", dateString, "Unreadable tag",
        null, "Description", new Counterparty(234, 789));
    var transferOut = new Transaction("Unreadable tag - transfer out", -4320, "GBP", dateString, "Unreadable tag",
        null, "Description", new Counterparty(234, 789));

    var result = UNDER_TEST.process(new TransactionList(transferIn, transferOut), ClientProcessingDetails.builder().create());

    checkForNulls(result);
    checkNoSuccessfulResults(result);
    checkForUnsuccessfulResults(Set.of(transferIn, transferOut), result);
    checkNoIgnoredTransactions(result);
  }

  @Test
  public void testTransfersSimpleTag() {
    var transferInResult = createTransferTransaction("Simple tag", Map.of("Tag1", 3.9), 390);
    var transferOutResult = createTransferTransaction("Simple tag", Map.of("Tag1", 32.7), -3270);

    var result = UNDER_TEST.process(new TransactionList(transferInResult.getOriginalTransaction(), transferOutResult.getOriginalTransaction()), ClientProcessingDetails.builder().create());

    checkForNulls(result);
    checkSuccessfulResult(result.getSuccessfulResults(), transferInResult, transferOutResult);
    checkNoUnsuccessfulResults(result);
    checkNoIgnoredTransactions(result);
  }

  @Test
  public void testTransfersComplexTag() {
    var transferInResult = createTransferTransaction("Complex tag", Map.of("Tag1", 53.4, "Tag2", 67.29, "Tag3", 11.11), 13180);
    var transferOutResult = createTransferTransaction("Complex tag", Map.of("Tag1", 53.4, "Tag2", 67.29, "Tag3", 11.11), -13180);

    var result = UNDER_TEST.process(new TransactionList(transferInResult.getOriginalTransaction(), transferOutResult.getOriginalTransaction()), ClientProcessingDetails.builder().create());

    checkForNulls(result);
    checkSuccessfulResult(result.getSuccessfulResults(), transferInResult, transferOutResult);
    checkNoUnsuccessfulResults(result);
    checkNoIgnoredTransactions(result);
  }

  @Test
  public void testTransferDefaultTag() {
    var accountNumber = 124;
    var sortCode = 876;
    var defaultTag = "DefaultTag";
    var clientDetails = ClientProcessingDetails.builder().addAutoTagAccount(accountNumber + "/" + sortCode, "#" + defaultTag).create();
    var originalTransaction = new Transaction("No tag", 10000, "GBP", "2018-02-03T08:00:00.0Z", "", new Merchant(),
        "Description", new Counterparty(accountNumber, sortCode));
    var expectedTransaction = new TransferTransaction("No tag", ZonedDateTime.of(2018, 2, 3, 8, 0, 0, 0, ZoneId.of("UTC")),
        new Money(10000, "GBP"), accountNumber + "/" + sortCode + " - Description", defaultTag);

    var result = UNDER_TEST.process(new TransactionList(originalTransaction), clientDetails);

    checkForNulls(result);
    checkSuccessfulResult(result.getSuccessfulResults(), new InputAndOutputTransactions(originalTransaction, expectedTransaction));
    checkNoUnsuccessfulResults(result);
    checkNoIgnoredTransactions(result);
  }

  @Test
  public void testTransferOverrideTagWithDefault() {
    var accountNumber = 124;
    var sortCode = 876;
    var defaultTag = "DefaultTag";
    var clientDetails = ClientProcessingDetails.builder().addAutoTagAccount(accountNumber + "/" + sortCode, "#" + defaultTag).create();
    var originalTransaction = new Transaction("Tag to override", 10000, "GBP", "2018-02-03T08:00:00.0Z", "#TagToOverride", new Merchant(),
        "Description", new Counterparty(accountNumber, sortCode));
    var expectedTransaction = new TransferTransaction("Tag to override", ZonedDateTime.of(2018, 2, 3, 8, 0, 0, 0, ZoneId.of("UTC")),
        new Money(10000, "GBP"), accountNumber + "/" + sortCode + " - Description", defaultTag);

    var result = UNDER_TEST.process(new TransactionList(originalTransaction), clientDetails);

    checkForNulls(result);
    checkSuccessfulResult(result.getSuccessfulResults(), new InputAndOutputTransactions(originalTransaction, expectedTransaction));
    checkNoUnsuccessfulResults(result);
    checkNoIgnoredTransactions(result);
  }

  @Test
  public void testUnimplementedTransactionTypes() {
    var dateString = "2018-01-03T08:00:00.0Z";
    var missingAllDetails = new Transaction("Bank transfer in", 420, "GBP", dateString, "Notes", null, "Description", emptyCounterparty());

    var result = UNDER_TEST.process(new TransactionList(missingAllDetails), ClientProcessingDetails.builder().create());

    checkForNulls(result);
    checkNoSuccessfulResults(result);
    checkForUnsuccessfulResults(Set.of(missingAllDetails), result);
    checkNoIgnoredTransactions(result);
  }

  private static void checkForUnsuccessfulResults(Set<Transaction> inputSet, TransactionProcessorResult result) {
    var unsuccessfulTransactions = result.getUnsuccessfulResults();
    assertEquals("Should have same number of unsuccessful transactions as input", inputSet.size(), unsuccessfulTransactions.size());
    assertTrue("Inputs missing or changed", unsuccessfulTransactions.keySet().containsAll(inputSet));
  }

  private static Transaction createSimpleSaleTransaction(String testName, String notes, int totalAmount) {
    var dateString = "2018-01-02T08:00:00.0Z";
    return new Transaction(testName, totalAmount, "GBP", dateString, notes, new Merchant("A Merchant"), "Description", emptyCounterparty());
  }

  private static void checkNoUnsuccessfulResults(TransactionProcessorResult result) {
    assertTrue("There should be no unsuccessful results", result.getUnsuccessfulResults().isEmpty());
  }

  private static void checkNoSuccessfulResults(TransactionProcessorResult result) {
    assertTrue("There should be no successful results", result.getSuccessfulResults().isEmpty());
  }

  private static void checkNoIgnoredTransactions(TransactionProcessorResult result) {
    assertTrue("There should be no ignored transactions", result.getIgnoredTransactions().isEmpty());
  }

  private static void checkSuccessfulResult(Map<Transaction, Set<? extends ProcessedTransaction>> outputResults, InputAndOutputTransactions... expectedResults) {
    assertEquals("There should be " + expectedResults.length + " successful results", expectedResults.length, outputResults.size());
    Stream.of(expectedResults).forEach(expResult -> findAndCheckTransaction(expResult.getOriginalTransaction(), expResult.getProcessedTransactions(), outputResults));
  }

  private static void findAndCheckTransaction(Transaction inputTransaction, Collection<? extends ProcessedTransaction> expectedOutputTransactions, Map<Transaction, Set<? extends ProcessedTransaction>> results) {
    assertTrue(inputTransaction.getId() + " should have an entry in the successful results", results.containsKey(inputTransaction));
    var processedResults = results.get(inputTransaction);
    assertEquals("Should have " + expectedOutputTransactions.size() + " processed transactions for input " + inputTransaction.getId(),
        expectedOutputTransactions.size(), processedResults.size());
    assertTrue("Difference in processed transactions for input " + inputTransaction.getId(), processedResults.containsAll(expectedOutputTransactions));
  }

  private static InputAndOutputTransactions createSingleSaleTransaction() {
    return createSingleSaleTransaction(1);
  }

  private static InputAndOutputTransactions createSingleSaleTransaction(int seed) {
    return createSaleTransactions(seed, Map.of("TestTag", seed * 520));
  }

  private static InputAndOutputTransactions createSaleTransactions(int seed, Map<String, Integer> tagsAndAmounts) {
    return createSaleTransactions(seed, tagsAndAmounts, Optional.empty());
  }

  private static InputAndOutputTransactions createSaleTransactions(int seed, Map<String, Integer> tagsAndAmounts, Optional<Integer> restAmount) {
    var merchantName = "MERCHANT_NAME" + seed;
    var merchant = new Merchant(merchantName);
    var id = "BUY_TRANSACTION_ID" + seed;
    var currency = "GBP";
    var nanosNow = ZonedDateTime.now().getNano();
    var dateString = "2018-01-02T07:52:54." + nanosNow + "Z";
    var date = ZonedDateTime.of(2018, 1, 2, 7, 52, 54, nanosNow, ZoneId.of("UTC"));
    var notes = tagsAndAmounts.keySet().stream()
        .map(tag -> "" + tagsAndAmounts.get(tag) + " #" + tag)
        .collect(Collectors.joining("; "));
    if(restAmount.isPresent()) {
      notes = notes + "; rest #RestTag";
    }
    var description = "DESCRIPTION" + seed;

    var allTagsAndAmounts = Maps.newHashMap(tagsAndAmounts);
    restAmount.ifPresent(amount -> allTagsAndAmounts.put("RestTag", amount));
    var totalAmount = -100 * allTagsAndAmounts.values().stream().mapToInt(Integer::intValue).sum();

    var inputTransaction = new Transaction(id, totalAmount, currency, dateString, notes, merchant, description, emptyCounterparty());
    var outputTransactions = allTagsAndAmounts.entrySet().stream()
        .map(tagAndAmount -> new SaleTransaction(id, date, new Money(-100 * tagAndAmount.getValue(), currency), merchantName, tagAndAmount.getKey()))
        .collect(toList());
    return new InputAndOutputTransactions(inputTransaction, outputTransactions);
  }

  private static InputAndOutputTransactions createPotTransaction(String potId, String tag, boolean isIn) {
    return createPotTransaction(potId, tag, isIn, isIn ? 230 : -410);
  }

  private static InputAndOutputTransactions createPotTransaction(String potId, String tag, boolean isIn, int amount) {
    assertTrue("Pot ID must start with pot_", potId.startsWith(POT_PREFIX));
    var dateString = "2018-01-04T08:00:00.0Z";
    var date = ZonedDateTime.of(2018, 1, 4, 8, 0, 0, 0, ZoneId.of("UTC"));
    var inOrOut = isIn ? "in" : "out";
    var transactionId = "Pot transfer " + inOrOut;
    var inputTransaction = new Transaction(transactionId, amount, "GBP", dateString, "Notes", new Merchant(), potId, emptyCounterparty());
    var processedTransaction = new TransferTransaction(transactionId, date, new Money(amount, "GBP"), potId, tag);
    return new InputAndOutputTransactions(inputTransaction, Set.of(processedTransaction));
  }

  private static InputAndOutputTransactions createTransferTransaction(String testName, Map<String, Double> tagsAndAmounts, int totalAmount) {
    var dateString = "2018-01-09T08:30:00.0Z";
    var date = ZonedDateTime.of(2018, 1, 9, 8, 30, 0, 0, ZoneId.of("UTC"));
    var notes = tagsAndAmounts.entrySet().stream().map(e -> e.getValue() + " #" + e.getKey()).collect(joining(";"));
    var isIn = totalAmount > 0;
    var transactionId = testName + (isIn ? " in" : " out");
    var counterparty = new Counterparty(123, 456);
    var description = "Description";
    var inputTransaction = new Transaction(transactionId, totalAmount, "GBP", dateString, notes, new Merchant(), description, counterparty);
    var expectedWhere = counterparty.getAccountNumber() + "/" + counterparty.getSortCode() + " - " + description;
    var processedTransactions = tagsAndAmounts.entrySet().stream()
        .map(e -> new TransferTransaction(transactionId, date, new Money(isIn ? convertToPence(e.getValue()) : -1 * convertToPence(e.getValue()), "GBP"), expectedWhere, e.getKey()))
        .collect(toSet());
    return new InputAndOutputTransactions(inputTransaction, processedTransactions);
  }

  private static int convertToPence(double poundAmount) {
    return (int) Math.round(poundAmount * 100d);
  }

  private static void checkForNulls(TransactionProcessorResult result) {
    assertNotNull("Should return non null result", result);
    assertNotNull("Should return non null successful results list", result.getSuccessfulResults());
    assertNotNull("Should return non null unsuccessful results list", result.getUnsuccessfulResults());
    assertNotNull("Should return non null ignored transactions", result.getIgnoredTransactions());
  }

  private static void checkIgnoredTransactions(TransactionProcessorResult result, Transaction... expectedIgnoredTransactions) {
    var ignoredTransactions = result.getIgnoredTransactions();
    Stream.of(expectedIgnoredTransactions).forEach(t -> assertTrue("Should have ignored result for " + t.getId(), ignoredTransactions.contains(t)));
    assertEquals("Should have only " + expectedIgnoredTransactions.length + " ignored transactions", expectedIgnoredTransactions.length, ignoredTransactions.size());
  }

  private static Counterparty emptyCounterparty() {
    return new Counterparty(null, null);
  }

  private static class InputAndOutputTransactions extends Pair<Transaction, Collection<? extends ProcessedTransaction>> {
    InputAndOutputTransactions(Transaction transaction, Collection<? extends ProcessedTransaction> processedTransactions) {
      super(transaction, processedTransactions);
    }

    InputAndOutputTransactions(Transaction transaction, ProcessedTransaction processedTransaction) {
      super(transaction, Set.of(processedTransaction));
    }

    Transaction getOriginalTransaction() {
      return getA();
    }

    Collection<? extends ProcessedTransaction> getProcessedTransactions() {
      return getB();
    }
  }
}