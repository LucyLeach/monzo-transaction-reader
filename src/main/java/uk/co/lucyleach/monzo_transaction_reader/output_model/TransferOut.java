package uk.co.lucyleach.monzo_transaction_reader.output_model;

import java.time.ZonedDateTime;

/**
 * User: Lucy
 * Date: 29/07/2018
 * Time: 21:11
 */
public class TransferOut extends ProcessedTransaction
{
  private final String whereTransferredTo;
  private final String tag;

  public TransferOut(String monzoId, ZonedDateTime dateTime, Money amount, String whereTransferredTo, String tag)
  {
    super(monzoId, dateTime, amount);
    this.whereTransferredTo = whereTransferredTo;
    this.tag = tag;
  }

  public String getWhereTransferredTo()
  {
    return whereTransferredTo;
  }

  public String getTag()
  {
    return tag;
  }
}
