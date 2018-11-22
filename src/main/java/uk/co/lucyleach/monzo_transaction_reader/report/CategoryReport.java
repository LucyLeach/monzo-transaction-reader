package uk.co.lucyleach.monzo_transaction_reader.report;

import java.util.List;

/**
 * User: Lucy
 * Date: 01/10/2018
 * Time: 20:27
 */
public class CategoryReport {
  private final String category;
  private final List<Double> amountOutByMonth;

  public CategoryReport(String category, List<Double> amountOutByMonth) {
    this.category = category;
    this.amountOutByMonth = List.copyOf(amountOutByMonth);
  }

  public String getCategory() {
    return category;
  }

  public List<Double> getAmountOutByMonth() {
    return amountOutByMonth;
  }
}
