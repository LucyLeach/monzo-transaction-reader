package uk.co.lucyleach.monzo_transaction_reader.report;

import uk.co.lucyleach.monzo_transaction_reader.output_model.Money;
import uk.co.lucyleach.monzo_transaction_reader.output_model.ProcessedTransaction;

import java.util.List;
import java.util.Objects;

/**
 * User: Lucy
 * Date: 07/09/2018
 * Time: 20:57
 */
public class TagLevelReport {
  private final String tag;
  private final String tagCategory;
  private final Money totalAmountIn;
  private final Money totalAmountOut;
  private final List<ProcessedTransaction> contributingTransactions;

  public TagLevelReport(String tag, String tagCategory, Money totalAmountIn, Money totalAmountOut, List<ProcessedTransaction> contributingTransactions) {
    this.tag = tag;
    this.tagCategory = tagCategory;
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

  public String getTagCategory() {
    return tagCategory == null ? tag : tagCategory;
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

  public List<ProcessedTransaction> getContributingTransactions() {
    return contributingTransactions;
  }

  @Override
  public String toString() {
    return tag + ": " + contributingTransactions.size() + " transactions, totalling " + getTotalAmount();
  }

  @Override
  public boolean equals(Object o) {
    if(this == o) return true;
    if(o == null || getClass() != o.getClass()) return false;
    TagLevelReport that = (TagLevelReport) o;
    return Objects.equals(tag, that.tag) &&
        Objects.equals(tagCategory, that.tagCategory) &&
        Objects.equals(totalAmountIn, that.totalAmountIn) &&
        Objects.equals(totalAmountOut, that.totalAmountOut) &&
        Objects.equals(contributingTransactions, that.contributingTransactions);
  }

  @Override
  public int hashCode() {
    return Objects.hash(tag, tagCategory, totalAmountIn, totalAmountOut, contributingTransactions);
  }
}
