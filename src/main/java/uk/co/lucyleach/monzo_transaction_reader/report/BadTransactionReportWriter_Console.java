package uk.co.lucyleach.monzo_transaction_reader.report;

import uk.co.lucyleach.monzo_transaction_reader.monzo_model.Transaction;

import java.util.Map;

/**
 * User: Lucy
 * Date: 15/11/2018
 * Time: 19:36
 */
public class BadTransactionReportWriter_Console {
  public void write(Map<Transaction, String> transactionsAndErrors) {
    transactionsAndErrors.forEach((transaction, errorMessage) -> {
      System.out.println("Error: " + errorMessage);
      System.out.println(transaction.toString());
    });
  }
}
