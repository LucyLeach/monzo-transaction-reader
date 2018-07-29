package uk.co.lucyleach.monzo_transaction_reader.output_model;

import java.time.LocalDateTime;

/**
 * User: Lucy
 * Date: 29/07/2018
 * Time: 21:10
 */
public class TransferIn extends ProcessedTransaction
{
  private final String whereTransferredFrom;
  private final boolean expectedTransfer;

  public TransferIn(String monzoId, LocalDateTime dateTime, Money amount, String whereTransferredFrom, boolean expectedTransfer)
  {
    super(monzoId, dateTime, amount);
    this.whereTransferredFrom = whereTransferredFrom;
    this.expectedTransfer = expectedTransfer;
  }

  public String getWhereTransferredFrom()
  {
    return whereTransferredFrom;
  }

  public boolean isExpectedTransfer()
  {
    return expectedTransfer;
  }
}
