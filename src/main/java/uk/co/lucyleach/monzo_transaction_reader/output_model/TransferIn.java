package uk.co.lucyleach.monzo_transaction_reader.output_model;

import java.time.ZonedDateTime;

/**
 * User: Lucy
 * Date: 29/07/2018
 * Time: 21:10
 */
public class TransferIn extends ProcessedTransaction {
  private final String whereTransferredFrom;
  private final boolean expectedTransfer;
  private final String tag;

  public TransferIn(String monzoId, ZonedDateTime dateTime, Money amount, String whereTransferredFrom, boolean expectedTransfer, String tag) {
    super(monzoId, dateTime, amount);
    this.whereTransferredFrom = whereTransferredFrom;
    this.expectedTransfer = expectedTransfer;
    this.tag = tag;
  }

  public String getWhereTransferredFrom() {
    return whereTransferredFrom;
  }

  public boolean isExpectedTransfer() {
    return expectedTransfer;
  }

  public String getTag() {
    return tag;
  }
}
