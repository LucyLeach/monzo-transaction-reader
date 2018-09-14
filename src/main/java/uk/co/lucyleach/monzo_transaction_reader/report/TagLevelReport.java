package uk.co.lucyleach.monzo_transaction_reader.report;

import uk.co.lucyleach.monzo_transaction_reader.output_model.Money;
import uk.co.lucyleach.monzo_transaction_reader.output_model.ProcessedTransaction;

import java.util.List;

/**
 * User: Lucy
 * Date: 07/09/2018
 * Time: 20:57
 */
public class TagLevelReport {
  private final String tag;
  private final Money totalAmountIn;
  private final Money totalAmountOut;
  private final List<ProcessedTransaction> contributingTransactions;

  public TagLevelReport(String tag, Money totalAmountIn, Money totalAmountOut, List<ProcessedTransaction> contributingTransactions) {
    this.tag = tag;
    this.totalAmountIn = totalAmountIn;
    this.totalAmountOut = totalAmountOut;
    this.contributingTransactions = List.copyOf(contributingTransactions);
  }

  public Money getTotalAmount() {
    return totalAmountIn.add(totalAmountOut);
  }

  public String getTag() {
    return tag;
  }

  public Money getTotalAmountIn() {
    return totalAmountIn;
  }

  public Money getTotalAmountOut() {
    return totalAmountOut;
  }

  public int getNumTransactions() {
    return contributingTransactions.size();
  }

  @Override
  public String toString() {
    return tag + ": " + contributingTransactions.size() + " transactions, totalling " + getTotalAmount();
  }
}
