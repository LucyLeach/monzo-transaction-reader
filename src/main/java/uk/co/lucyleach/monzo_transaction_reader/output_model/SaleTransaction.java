package uk.co.lucyleach.monzo_transaction_reader.output_model;

import java.time.LocalDateTime;

/**
 * User: Lucy
 * Date: 29/07/2018
 * Time: 21:09
 */
public class SaleTransaction extends ProcessedTransaction
{
  private final String merchantName;
  private final String tag;

  public SaleTransaction(String monzoId, LocalDateTime dateTime, Money amount, String merchantName, String tag)
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
}
