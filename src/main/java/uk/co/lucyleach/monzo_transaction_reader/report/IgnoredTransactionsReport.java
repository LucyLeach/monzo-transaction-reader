package uk.co.lucyleach.monzo_transaction_reader.report;

import uk.co.lucyleach.monzo_transaction_reader.monzo_model.Transaction;
import uk.co.lucyleach.monzo_transaction_reader.output_model.Money;
import uk.co.lucyleach.monzo_transaction_reader.processor.ReasonIgnored;

import java.util.List;

/**
 * User: Lucy
 * Date: 08/09/2018
 * Time: 20:24
 */
public class IgnoredTransactionsReport {
  private final ReasonIgnored reasonIgnored;
  private final Money totalAmountIn;
  private final Money totalAmountOut;
  private final List<Transaction> ignoredTransactions;

  public IgnoredTransactionsReport(ReasonIgnored reasonIgnored, Money totalAmountIn, Money totalAmountOut, List<Transaction> ignoredTransactions) {
    this.reasonIgnored = reasonIgnored;
    this.totalAmountIn = totalAmountIn;
    this.totalAmountOut = totalAmountOut;
    this.ignoredTransactions = List.copyOf(ignoredTransactions);
  }

  public Money getTotalAmount() {
    return totalAmountIn.add(totalAmountOut);
  }

  public ReasonIgnored getReasonIgnored() {
    return reasonIgnored;
  }

  public List<Transaction> getIgnoredTransactions() {
    return ignoredTransactions;
  }

  @Override
  public String toString() {
    return reasonIgnored + ": " + ignoredTransactions.size() + " transactions, totalling " + getTotalAmount();
  }
}
