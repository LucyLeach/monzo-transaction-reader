package uk.co.lucyleach.monzo_transaction_reader.processor;

import org.junit.Test;
import uk.co.lucyleach.monzo_transaction_reader.monzo_model.Merchant;
import uk.co.lucyleach.monzo_transaction_reader.monzo_model.Transaction;
import uk.co.lucyleach.monzo_transaction_reader.monzo_model.TransactionList;
import uk.co.lucyleach.monzo_transaction_reader.output_model.Money;
import uk.co.lucyleach.monzo_transaction_reader.output_model.SaleTransaction;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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
    assertEquals("There should be no unsuccessful results", 0, result.getUnsuccessfulResults().size());
  }

  //@Test
  public void testSingleSaleTransaction() {
    var inputOutputPair = createSingleSaleTransaction();
    var inputTransaction = inputOutputPair.getA();
    var expectedOutputTransaction = inputOutputPair.getB();

    var result = UNDER_TEST.process(new TransactionList(inputTransaction));

    checkForNulls(result);
    assertEquals("There should be no unsuccessful results", 0, result.getUnsuccessfulResults().size());
    assertEquals("There should be one successful results", 1, result.getSuccessfulResults().size());
    var singleResult = result.getSuccessfulResults().iterator().next();
    assertEquals("Successful result should be for input transaction", inputTransaction, singleResult.getOriginalTransaction());
    assertEquals("Should only have created one processed transaction", 1, singleResult.getProcessedTransactions().size());
    assertEquals(expectedOutputTransaction, singleResult.getProcessedTransactions().iterator().next());
  }

  private static Pair<Transaction, SaleTransaction> createSingleSaleTransaction() {
    return createSingleSaleTransaction(1);
  }

  private static Pair<Transaction, SaleTransaction> createSingleSaleTransaction(int seed) {
    var merchantName = "MERCHANT_NAME" + seed;
    var merchant = new Merchant(merchantName);
    var id = "BUY_TRANSACTION_ID" + seed;
    var amount = 500 * seed;
    var currency = "GBP";
    var nanosNow = ZonedDateTime.now().getNano();
    var dateString = "2018-01-02T07:52:54." + nanosNow + "Z";
    var date = ZonedDateTime.of(2018, 1, 2, 7, 52, 54, nanosNow, ZoneId.of("UTC"));
    var tag = "TestTag" + seed;
    var notes = "#" + tag;
    var description = "DESCRIPTION" + seed;

    var saleTransaction = new Transaction(id, amount, currency, dateString, notes, merchant, description, null);
    var processedSaleTransaction = new SaleTransaction(id, date, new Money(amount, currency), merchantName, notes);

    return new Pair<>(saleTransaction, processedSaleTransaction);
  }

  private static void checkForNulls(TransactionProcessorResult result)
  {
    assertNotNull("Should return non null result", result);
    assertNotNull("Should return non null successful results list", result.getSuccessfulResults());
    assertNotNull("Should return non null unsuccessful results list", result.getUnsuccessfulResults());
  }
}