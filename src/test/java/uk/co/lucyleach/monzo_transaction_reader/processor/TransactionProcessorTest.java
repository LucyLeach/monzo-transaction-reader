package uk.co.lucyleach.monzo_transaction_reader.processor;

import org.junit.Test;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.junit.Assert.*;

/**
 * User: Lucy
 * Date: 05/08/2018
 * Time: 20:37
 */
public class TransactionProcessorTest
{
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
  public void testImpossibleTagsFail() {
    fail();
  }

  @Test
  public void testUnimplementedTransactions() {
    fail();
  }

  private static void checkNoUnsuccessfulResults(TransactionProcessorResult result)
  {
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
    var description = "DESCRIPTION" + seed;
    var totalAmount = tagsAndAmounts.values().stream().mapToInt(Integer::intValue).sum();

    var inputTransaction = new Transaction(id, totalAmount, currency, dateString, notes, merchant, description, null);
    var outputTransactions = tagsAndAmounts.entrySet().stream()
        .map(tagAndAmount -> new SaleTransaction(id, date, new Money(tagAndAmount.getValue(), currency), merchantName, tagAndAmount.getKey()))
        .collect(toList());
    return new SuccessfulProcessorResult(inputTransaction, outputTransactions);
  }

  private static void checkForNulls(TransactionProcessorResult result)
  {
    assertNotNull("Should return non null result", result);
    assertNotNull("Should return non null successful results list", result.getSuccessfulResults());
    assertNotNull("Should return non null unsuccessful results list", result.getUnsuccessfulResults());
  }
}