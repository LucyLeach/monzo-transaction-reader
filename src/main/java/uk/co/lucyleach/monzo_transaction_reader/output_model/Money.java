package uk.co.lucyleach.monzo_transaction_reader.output_model;

/**
 * User: Lucy
 * Date: 29/07/2018
 * Time: 21:03
 */
public class Money
{
  private final int amountInPence;
  private final String currency;

  public Money(int amountInPence, String currency)
  {
    this.amountInPence = amountInPence;
    this.currency = currency;
  }

  public int getAmountInPence()
  {
    return amountInPence;
  }

  public String getCurrency()
  {
    return currency;
  }
}
