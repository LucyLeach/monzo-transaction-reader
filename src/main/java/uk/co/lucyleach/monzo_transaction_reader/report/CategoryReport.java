package uk.co.lucyleach.monzo_transaction_reader.report;

import uk.co.lucyleach.monzo_transaction_reader.output_model.Money;

import java.util.List;

/**
 * User: Lucy
 * Date: 01/10/2018
 * Time: 20:27
 */
public class CategoryReport {
  private final String category;
  private final List<Money> amountOutBySplit;

  public CategoryReport(String category, List<Money> amountOutBySplit) {
    this.category = category;
    this.amountOutBySplit = List.copyOf(amountOutBySplit);
  }

  public String getCategory() {
    return category;
  }

  public List<Money> getAmountOutBySplit() {
    return amountOutBySplit;
  }
}
