package uk.co.lucyleach.monzo_transaction_reader.output_model;

import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * User: Lucy
 * Date: 29/07/2018
 * Time: 21:11
 */
public class TransferTransaction extends ProcessedTransaction {
  private final String whereTransferredTo;

  public TransferTransaction(String monzoId, ZonedDateTime dateTime, Money amount, String whereTransferredTo, String tag) {
    super(monzoId, dateTime, amount, tag);
    this.whereTransferredTo = whereTransferredTo;
  }

  public String getWhere() {
    return whereTransferredTo;
  }

  @Override
  public String toString() {
    return "TransferTransaction{" + toStringOnFields() +
        ", " + "whereTransferredTo='" + whereTransferredTo + '\'' +
        '}';
  }

  @Override
  public boolean equals(Object o) {
    if(this == o) return true;
    if(o == null || getClass() != o.getClass()) return false;
    if(!super.equals(o)) return false;
    var that = (TransferTransaction) o;
    return Objects.equals(whereTransferredTo, that.whereTransferredTo);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), whereTransferredTo);
  }
}
