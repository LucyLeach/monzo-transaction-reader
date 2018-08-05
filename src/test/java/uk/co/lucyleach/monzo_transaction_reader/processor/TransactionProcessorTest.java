package uk.co.lucyleach.monzo_transaction_reader.processor;

import org.junit.Test;
import uk.co.lucyleach.monzo_transaction_reader.monzo_model.Merchant;
import uk.co.lucyleach.monzo_transaction_reader.monzo_model.Transaction;
import uk.co.lucyleach.monzo_transaction_reader.monzo_model.TransactionList;

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

  @Test
  public void testSingleBuyTransaction() {
    var merchant = new Merchant("MERCHANT_NAME");
    var buyTransaction = new Transaction("BUY_TRANSACTION_ID", 500, "GBP", "2018-01-02T07:52:54.762Z", "", merchant, "DESCRIPTION", null);
    var result = UNDER_TEST.process(new TransactionList(buyTransaction));
    checkForNulls(result);
    assertEquals("There should be no successful results", 0, result.getSuccessfulResults().size());
    assertEquals("There should be one unsuccessful result", 1, result.getUnsuccessfulResults().size());
    assertEquals("Unsuccessful result should be for input transaction", buyTransaction, result.getUnsuccessfulResults().iterator().next().getOriginalTransaction());
  }

  private static void checkForNulls(TransactionProcessorResult result)
  {
    assertNotNull("Should return non null result", result);
    assertNotNull("Should return non null successful results list", result.getSuccessfulResults());
    assertNotNull("Should return non null unsuccessful results list", result.getUnsuccessfulResults());
  }
}