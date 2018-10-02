package uk.co.lucyleach.monzo_transaction_reader.report;

import java.util.List;

/**
 * User: Lucy
 * Date: 01/10/2018
 * Time: 20:27
 */
public class CategoryReport {
  private final String category;
  private final List<Double> amountOutBySplit;

  public CategoryReport(String category, List<Double> amountOutBySplit) {
    this.category = category;
    this.amountOutBySplit = List.copyOf(amountOutBySplit);
  }

  public String getCategory() {
    return category;
  }

  public List<Double> getAmountOutBySplit() {
    return amountOutBySplit;
  }
}
