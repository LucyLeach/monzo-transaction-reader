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
    var result = UNDER_TEST.process(new TransactionList());
    checkForNulls(result);
    assertEquals("There should be no successful results", 0, result.getSuccessfulResults().size());
    checkNoUnsuccessfulResults(result);
  }

  @Test
  public void testSingleSaleTransaction() {
    var expectedResult = createSingleSaleTransaction();

    var result = UNDER_TEST.process(new TransactionList(expectedResult.getOriginalTransaction()));

    checkForNulls(result);
    checkNoUnsuccessfulResults(result);
    testSuccessfulResult(result.getSuccessfulResults(), expectedResult);
  }

  @Test
  public void testMultipleTransactions() {
    var expectedResult1 = createSingleSaleTransaction(1);
    var expectedResult2 = createSingleSaleTransaction(2);

    var result = UNDER_TEST.process(new TransactionList(expectedResult1.getA(), expectedResult2.getA()));

    checkForNulls(result);
    checkNoUnsuccessfulResults(result);
    testSuccessfulResult(result.getSuccessfulResults(), expectedResult1, expectedResult2);
  }

  @Test
  public void testMultiTagTransaction() {
    var tagsToAmounts = Map.of("Tag1", 350, "Tag2", 600, "Tag3", 210);
    var expectedSuccessfulResult = createSaleTransactions(1, tagsToAmounts);

    var result = UNDER_TEST.process(new TransactionList(expectedSuccessfulResult.getOriginalTransaction()));

    checkForNulls(result);
    checkNoUnsuccessfulResults(result);
    testSuccessfulResult(result.getSuccessfulResults(), expectedSuccessfulResult);
  }

  @Test
  public void testMultiTagIncludingRest() {
    var tagsToAmounts = Map.of("Tag1", 378, "Tag2", 890);
    var expectedSuccessfulResult = createSaleTransactions(1, tagsToAmounts, Optional.of(310));

    var result = UNDER_TEST.process(new TransactionList(expectedSuccessfulResult.getOriginalTransaction()));

    checkForNulls(result);
    checkNoUnsuccessfulResults(result);
    testSuccessfulResult(result.getSuccessfulResults(), expectedSuccessfulResult);
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

    var result = UNDER_TEST.process(new TransactionList(inputWithoutHashes));

    checkForNulls(result);
    checkNoUnsuccessfulResults(result);
    testSuccessfulResult(result.getSuccessfulResults(), expectedSuccessfulResultWoHashes);
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

    var result = UNDER_TEST.process(new TransactionList(Lists.newArrayList(inputSet)));

    checkForNulls(result);
    assertEquals("Should have no successful results", 0, result.getSuccessfulResults().size());
    testForUnsuccessfulResults(inputSet, result);
  }

  @Test
  public void testUnimplementedTransactionTypes() {
    var dateString = "2018-01-03T08:00:00.0Z";
    var potTransferIn = new Transaction("Pot transfer in", 230, "GBP", dateString, "Notes", null, "pot_123", null);
    var potTransferOut = new Transaction("Pot transfer out", -230, "GBP", dateString, "Notes", null, "pot_123", null);
    var bankTransferIn = new Transaction("Bank transfer in", 420, "GBP", dateString, "Notes", null, "Description", new Counterparty(54398, 200000));
    var bankTransferOut = new Transaction("Bank transfer in", -420, "GBP", dateString, "Notes", null, "Description", new Counterparty(54398, 200000));

    var result = UNDER_TEST.process(new TransactionList(potTransferIn, potTransferOut, bankTransferIn, bankTransferOut));

    checkForNulls(result);
    assertEquals("Should have no successful results", 0, result.getSuccessfulResults().size());
    testForUnsuccessfulResults(Set.of(potTransferIn, potTransferOut, bankTransferIn, bankTransferOut), result);
  }

  private static void testForUnsuccessfulResults(Set<Transaction> inputSet, TransactionProcessorResult result) {
    var unsuccessfulTransactions = result.getUnsuccessfulResults().stream().map(UnsuccessfulProcessorResult::getOriginalTransaction).collect(toSet());
    assertEquals("Should have same number of unsuccessful transactions as input", inputSet.size(), unsuccessfulTransactions.size());
    assertTrue("Inputs missing or changed", unsuccessfulTransactions.containsAll(inputSet));
  }

  private static Transaction createSimpleSaleTransaction(String testName, String notes, int totalAmount) {
    var dateString = "2018-01-02T08:00:00.0Z";
    return new Transaction(testName, totalAmount, "GBP", dateString, notes, new Merchant("A Merchant"), "Description", null);
  }

  private static void checkNoUnsuccessfulResults(TransactionProcessorResult result) {
    assertEquals("There should be no unsuccessful results", 0, result.getUnsuccessfulResults().size());
  }

  private static void testSuccessfulResult(Collection<SuccessfulProcessorResult> outputResults, SuccessfulProcessorResult... expectedResults) {
    assertEquals("There should be " + expectedResults.length + " successful results", expectedResults.length, outputResults.size());
    Stream.of(expectedResults).forEach(expResult -> findAndTestTransaction(expResult.getOriginalTransaction(), expResult.getProcessedTransactions(), outputResults));
  }

  private static void findAndTestTransaction(Transaction inputTransaction, Collection<? extends ProcessedTransaction> expectedOutputTransactions, Collection<SuccessfulProcessorResult> results) {
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

  private static void checkForNulls(TransactionProcessorResult result) {
    assertNotNull("Should return non null result", result);
    assertNotNull("Should return non null successful results list", result.getSuccessfulResults());
    assertNotNull("Should return non null unsuccessful results list", result.getUnsuccessfulResults());
  }
}