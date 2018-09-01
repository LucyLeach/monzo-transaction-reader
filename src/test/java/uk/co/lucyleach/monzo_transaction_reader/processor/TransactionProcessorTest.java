package uk.co.lucyleach.monzo_transaction_reader.processor;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.junit.Ignore;
import org.junit.Test;
import uk.co.lucyleach.monzo_transaction_reader.monzo_model.Counterparty;
import uk.co.lucyleach.monzo_transaction_reader.monzo_model.Merchant;
import uk.co.lucyleach.monzo_transaction_reader.monzo_model.Transaction;
import uk.co.lucyleach.monzo_transaction_reader.monzo_model.TransactionList;
import uk.co.lucyleach.monzo_transaction_reader.output_model.*;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
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
    var result = UNDER_TEST.process(new TransactionList(), emptyClientDetails());
    checkForNulls(result);
    checkNoSuccessfulResults(result);
    checkNoUnsuccessfulResults(result);
    checkNoIgnoredTransactions(result);
  }

  @Test
  public void testSingleSaleTransaction() {
    var expectedResult = createSingleSaleTransaction();

    var result = UNDER_TEST.process(new TransactionList(expectedResult.getOriginalTransaction()), emptyClientDetails());

    checkForNulls(result);
    checkSuccessfulResult(result.getSuccessfulResults(), expectedResult);
    checkNoUnsuccessfulResults(result);
    checkNoIgnoredTransactions(result);
  }

  @Test
  public void testMultipleTransactions() {
    var expectedResult1 = createSingleSaleTransaction(1);
    var expectedResult2 = createSingleSaleTransaction(2);

    var result = UNDER_TEST.process(new TransactionList(expectedResult1.getA(), expectedResult2.getA()), emptyClientDetails());

    checkForNulls(result);
    checkSuccessfulResult(result.getSuccessfulResults(), expectedResult1, expectedResult2);
    checkNoUnsuccessfulResults(result);
    checkNoIgnoredTransactions(result);
  }

  @Test
  public void testMultiTagTransaction() {
    var tagsToAmounts = Map.of("Tag1", 350, "Tag2", 600, "Tag3", 210);
    var expectedSuccessfulResult = createSaleTransactions(1, tagsToAmounts);

    var result = UNDER_TEST.process(new TransactionList(expectedSuccessfulResult.getOriginalTransaction()), emptyClientDetails());

    checkForNulls(result);
    checkSuccessfulResult(result.getSuccessfulResults(), expectedSuccessfulResult);
    checkNoUnsuccessfulResults(result);
    checkNoIgnoredTransactions(result);
  }

  @Test
  public void testMultiTagIncludingRest() {
    var tagsToAmounts = Map.of("Tag1", 378, "Tag2", 890);
    var expectedSuccessfulResult = createSaleTransactions(1, tagsToAmounts, Optional.of(310));

    var result = UNDER_TEST.process(new TransactionList(expectedSuccessfulResult.getOriginalTransaction()), emptyClientDetails());

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
        originalTransaction.getCreated(), notesWithoutHashes, originalTransaction.getMerchant(), originalTransaction.getDescription(), null);
    var expectedSuccessfulResultWoHashes = new SuccessfulProcessorResult(inputWithoutHashes, expectedSuccessfulResult.getProcessedTransactions());

    var result = UNDER_TEST.process(new TransactionList(inputWithoutHashes), emptyClientDetails());

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

    var result = UNDER_TEST.process(new TransactionList(Lists.newArrayList(inputSet)), emptyClientDetails());

    checkForNulls(result);
    checkNoSuccessfulResults(result);
    checkForUnsuccessfulResults(inputSet, result);
    checkNoIgnoredTransactions(result);
  }

  @Test
  public void testUnrecognisedPot() {
    var potId = POT_PREFIX + "Unrecognized";
    var potTransferIn = createPotTransaction(potId, "tag", true).getOriginalTransaction();
    var potTransferOut = createPotTransaction(potId, "tag", false).getOriginalTransaction();

    var result = UNDER_TEST.process(new TransactionList(potTransferIn, potTransferOut), emptyClientDetails());

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

    var clientDetails = new ClientProcessingDetails(Map.of(potId, "MappedTag"), Map.of());

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

    var clientDetails = new ClientProcessingDetails(Map.of(), Map.of(potId, "MappedTag"));

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

    var clientDetails = new ClientProcessingDetails(Map.of(potId, "MappedTagIn"), Map.of(potId, "MappedTagOut"));

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

    var clientDetails = new ClientProcessingDetails(Map.of(potId, "MappedTagIn"), Map.of(potId, "MappedTagOut"));

    var result = UNDER_TEST.process(new TransactionList(saleTransaction, potTransferOutResult.getOriginalTransaction(), potTransferInResult.getOriginalTransaction()), clientDetails);

    checkForNulls(result);
    checkNoSuccessfulResults(result);
    checkNoUnsuccessfulResults(result);
    checkIgnoredTransactions(result, saleTransaction, potTransferInResult.getOriginalTransaction(), potTransferOutResult.getOriginalTransaction());
  }

  @Test
  public void testIgnoreTag() {
    var justIgnoreTag = createSimpleSaleTransaction("Just ignore tag", IGNORE_TAG, -2930);
    var ignoreTagMiddle = createSimpleSaleTransaction("Ignore tag middle", "2.3 #groceries; 4.1 " + IGNORE_TAG  + "; rest #toys", -1089);

    var potId = POT_PREFIX + "Ignore Tag";
    var dateString = "2018-01-07T08:00:00.0Z";
    var ignorePotInTransaction = new Transaction(potId + " in", 5631, "GBP", dateString, IGNORE_TAG, null, potId, null);
    var ignorePotOutTransaction = new Transaction(potId + " out", -5631, "GBP", dateString, IGNORE_TAG, null, potId, null);
    var clientDetails = new ClientProcessingDetails(Map.of(potId, "MappedTagIn"), Map.of(potId, "MappedTagOut"));

    var result = UNDER_TEST.process(new TransactionList(justIgnoreTag, ignoreTagMiddle, ignorePotInTransaction, ignorePotOutTransaction), clientDetails);

    checkForNulls(result);
    checkNoSuccessfulResults(result);
    checkNoUnsuccessfulResults(result);
    checkIgnoredTransactions(result, justIgnoreTag, ignoreTagMiddle, ignorePotInTransaction, ignorePotOutTransaction);
  }

  @Test
  public void testUnimplementedTransactionTypes() {
    var dateString = "2018-01-03T08:00:00.0Z";
    var bankTransferIn = new Transaction("Bank transfer in", 420, "GBP", dateString, "Notes", null, "Description", new Counterparty(54398, 200000));
    var bankTransferOut = new Transaction("Bank transfer in", -420, "GBP", dateString, "Notes", null, "Description", new Counterparty(54398, 200000));

    var result = UNDER_TEST.process(new TransactionList(bankTransferIn, bankTransferOut), emptyClientDetails());

    checkForNulls(result);
    checkNoSuccessfulResults(result);
    checkForUnsuccessfulResults(Set.of(bankTransferIn, bankTransferOut), result);
    checkNoIgnoredTransactions(result);
  }

  private static void checkForUnsuccessfulResults(Set<Transaction> inputSet, TransactionProcessorResult result) {
    var unsuccessfulTransactions = result.getUnsuccessfulResults().stream().map(UnsuccessfulProcessorResult::getOriginalTransaction).collect(toSet());
    assertEquals("Should have same number of unsuccessful transactions as input", inputSet.size(), unsuccessfulTransactions.size());
    assertTrue("Inputs missing or changed", unsuccessfulTransactions.containsAll(inputSet));
  }

  private static Transaction createSimpleSaleTransaction(String testName, String notes, int totalAmount) {
    var dateString = "2018-01-02T08:00:00.0Z";
    return new Transaction(testName, totalAmount, "GBP", dateString, notes, new Merchant("A Merchant"), "Description", null);
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

  private static void checkSuccessfulResult(Collection<SuccessfulProcessorResult> outputResults, SuccessfulProcessorResult... expectedResults) {
    assertEquals("There should be " + expectedResults.length + " successful results", expectedResults.length, outputResults.size());
    Stream.of(expectedResults).forEach(expResult -> findAndCheckTransaction(expResult.getOriginalTransaction(), expResult.getProcessedTransactions(), outputResults));
  }

  private static void findAndCheckTransaction(Transaction inputTransaction, Collection<? extends ProcessedTransaction> expectedOutputTransactions, Collection<SuccessfulProcessorResult> results) {
    var matchingResults = results.stream().filter(r -> r.getOriginalTransaction().equals(inputTransaction)).collect(toList());
    assertEquals(inputTransaction.getId() + " should have one entry in the successful results", 1, matchingResults.size());
    var singleSuccessfulResult = matchingResults.get(0);
    assertEquals("Should have " + expectedOutputTransactions.size() + " processed transactions for input " + inputTransaction.getId(),
        expectedOutputTransactions.size(), singleSuccessfulResult.getProcessedTransactions().size());
    assertTrue("Difference in processed transactions for input " + inputTransaction.getId(), singleSuccessfulResult.getProcessedTransactions().containsAll(expectedOutputTransactions));
  }

  private static SuccessfulProcessorResult createSingleSaleTransaction() {
    return createSingleSaleTransaction(1);
  }

  private static SuccessfulProcessorResult createSingleSaleTransaction(int seed) {
    return createSaleTransactions(seed, Map.of("TestTag", seed * 520));
  }

  private static SuccessfulProcessorResult createSaleTransactions(int seed, Map<String, Integer> tagsAndAmounts) {
    return createSaleTransactions(seed, tagsAndAmounts, Optional.empty());
  }

  private static SuccessfulProcessorResult createSaleTransactions(int seed, Map<String, Integer> tagsAndAmounts, Optional<Integer> restAmount) {
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

    var inputTransaction = new Transaction(id, totalAmount, currency, dateString, notes, merchant, description, null);
    var outputTransactions = allTagsAndAmounts.entrySet().stream()
        .map(tagAndAmount -> new SaleTransaction(id, date, new Money(-1 * tagAndAmount.getValue(), currency), merchantName, tagAndAmount.getKey()))
        .collect(toList());
    return new SuccessfulProcessorResult(inputTransaction, outputTransactions);
  }

  private static SuccessfulProcessorResult createPotTransaction(String potId, String tag, boolean isIn) {
    return createPotTransaction(potId, tag, isIn, isIn ? 230 : -410);
  }

  private static SuccessfulProcessorResult createPotTransaction(String potId, String tag, boolean isIn, int amount) {
    assertTrue("Pot ID must start with pot_", potId.startsWith(POT_PREFIX));
    var dateString = "2018-01-04T08:00:00.0Z";
    var date = ZonedDateTime.of(2018, 1, 4, 8, 0, 0, 0, ZoneId.of("UTC"));
    var inOrOut = isIn ? "in" : "out";
    var transactionId = "Pot transfer " + inOrOut;
    var inputTransaction = new Transaction(transactionId, amount, "GBP", dateString, "Notes", null, potId, null);
    ProcessedTransaction processedTransaction;
    if(isIn) {
      processedTransaction = new TransferIn(transactionId, date, new Money(amount, "GBP"), potId, false, tag);
    } else {
      processedTransaction = new TransferOut(transactionId, date, new Money(amount, "GBP"), potId, tag);
    }
    return new SuccessfulProcessorResult(inputTransaction, Set.of(processedTransaction));
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

  private static ClientProcessingDetails emptyClientDetails() {
    return new ClientProcessingDetails(Map.of(), Map.of());
  }
}