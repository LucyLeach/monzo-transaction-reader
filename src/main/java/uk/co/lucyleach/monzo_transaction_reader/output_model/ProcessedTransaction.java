package uk.co.lucyleach.monzo_transaction_reader.output_model;

import java.time.LocalDateTime;

/**
 * User: Lucy
 * Date: 29/07/2018
 * Time: 21:00
 */
public abstract class ProcessedTransaction
{
  private final String monzoId;
  private final LocalDateTime dateTime;
  private final Money amount;

  ProcessedTransaction(String monzoId, LocalDateTime dateTime, Money amount)
  {
    this.monzoId = monzoId;
    this.dateTime = dateTime;
    this.amount = amount;
  }

  public String getMonzoId()
  {
    return monzoId;
  }

  public LocalDateTime getDateTime()
  {
    return dateTime;
  }

  public Money getAmount()
  {
    return amount;
  }
}
