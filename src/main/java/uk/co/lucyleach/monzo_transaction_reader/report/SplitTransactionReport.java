package uk.co.lucyleach.monzo_transaction_reader.report;

import uk.co.lucyleach.monzo_transaction_reader.output_model.Money;
import uk.co.lucyleach.monzo_transaction_reader.output_model.ProcessedTransaction;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * User: Lucy
 * Date: 16/09/2018
 * Time: 10:37
 */
public class SplitTransactionReport {
  private final List<ProcessedTransaction> plainListOfTransactions;
  private final Money totalAmountIn;
  private final Money totalAmountOut;
  private final List<TagLevelReport> tagReports;
  private final SortedMap<LocalDate, Money> expenditureByDate;

  public SplitTransactionReport(List<ProcessedTransaction> plainListOfTransactions, Money totalAmountIn, Money totalAmountOut,
                                List<TagLevelReport> tagReports, SortedMap<LocalDate, Money> expenditureByDate) {
    this.plainListOfTransactions = List.copyOf(plainListOfTransactions);
    this.totalAmountIn = totalAmountIn;
    this.totalAmountOut = totalAmountOut;
    this.tagReports = List.copyOf(tagReports);
    this.expenditureByDate = new TreeMap<>(expenditureByDate);
  }

  public ZonedDateTime getEarliestTransaction() {
    return plainListOfTransactions.get(0).getDateTime();
  }

  public List<ProcessedTransaction> getTransactions() {
    return plainListOfTransactions;
  }

  public Money getTotalAmountIn() {
    return totalAmountIn;
  }

  public Money getTotalAmountOut() {
    return totalAmountOut;
  }

  public List<TagLevelReport> getTagReports() {
    return tagReports;
  }

  public SortedMap<LocalDate, Money> getExpenditureByDate() {
    return new TreeMap<>(expenditureByDate);
  }
}
