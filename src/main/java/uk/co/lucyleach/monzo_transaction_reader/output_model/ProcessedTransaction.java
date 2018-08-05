package uk.co.lucyleach.monzo_transaction_reader.output_model;

import java.time.ZonedDateTime;

/**
 * User: Lucy
 * Date: 29/07/2018
 * Time: 21:00
 */
public abstract class ProcessedTransaction
{
  private final String monzoId;
  private final ZonedDateTime dateTime;
  private final Money amount;

  ProcessedTransaction(String monzoId, ZonedDateTime dateTime, Money amount)
  {
    this.monzoId = monzoId;
    this.dateTime = dateTime;
    this.amount = amount;
  }

  public String getMonzoId()
  {
    return monzoId;
  }

  public ZonedDateTime getDateTime()
  {
    return dateTime;
  }

  public Money getAmount()
  {
    return amount;
  }
}
