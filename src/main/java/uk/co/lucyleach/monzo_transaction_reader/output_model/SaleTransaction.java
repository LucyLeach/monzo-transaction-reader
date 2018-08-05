package uk.co.lucyleach.monzo_transaction_reader.output_model;

import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * User: Lucy
 * Date: 29/07/2018
 * Time: 21:09
 */
public class SaleTransaction extends ProcessedTransaction
{
  private final String merchantName;
  private final String tag;

  public SaleTransaction(String monzoId, ZonedDateTime dateTime, Money amount, String merchantName, String tag)
  {
    super(monzoId, dateTime, amount);
    this.merchantName = merchantName;
    this.tag = tag;
  }

  public String getMerchantName()
  {
    return merchantName;
  }

  public String getTag()
  {
    return tag;
  }

  @Override
  public boolean equals(Object o)
  {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    var that = (SaleTransaction) o;
    return Objects.equals(merchantName, that.merchantName) &&
        Objects.equals(tag, that.tag);
  }

  @Override
  public int hashCode()
  {
    return Objects.hash(merchantName, tag);
  }
}
