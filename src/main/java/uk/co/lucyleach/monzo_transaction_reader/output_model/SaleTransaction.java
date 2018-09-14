package uk.co.lucyleach.monzo_transaction_reader.output_model;

import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * User: Lucy
 * Date: 29/07/2018
 * Time: 21:09
 */
public class SaleTransaction extends ProcessedTransaction {
  private final String merchantName;

  public SaleTransaction(String monzoId, ZonedDateTime dateTime, Money amount, String merchantName, String tag) {
    super(monzoId, dateTime, amount, tag);
    this.merchantName = merchantName;
  }

  public String getWhere() {
    return merchantName;
  }

  @Override
  public String toString() {
    return "SaleTransaction{" + toStringOnFields() +
        ", " + "merchantName='" + merchantName + '\'' +
        '}';
  }

  @Override
  public boolean equals(Object o) {
    if(this == o) return true;
    if(o == null || getClass() != o.getClass()) return false;
    if(!super.equals(o)) return false;
    var that = (SaleTransaction) o;
    return Objects.equals(merchantName, that.merchantName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), merchantName);
  }


}
