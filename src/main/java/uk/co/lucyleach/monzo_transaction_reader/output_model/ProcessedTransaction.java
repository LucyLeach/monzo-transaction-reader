package uk.co.lucyleach.monzo_transaction_reader.output_model;

import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * User: Lucy
 * Date: 29/07/2018
 * Time: 21:00
 */
public abstract class ProcessedTransaction {
  private final String monzoId;
  private final ZonedDateTime dateTime;
  private final Money amount;
  private final String tag;

  ProcessedTransaction(String monzoId, ZonedDateTime dateTime, Money amount, String tag) {
    this.monzoId = monzoId;
    this.dateTime = dateTime;
    this.amount = amount;
    this.tag = tag;
  }

  public String getMonzoId() {
    return monzoId;
  }

  public ZonedDateTime getDateTime() {
    return dateTime;
  }

  public Money getAmount() {
    return amount;
  }

  public String getTag() {
    return tag;
  }

  public boolean isPositive() {
    return amount.isPositive();
  }

  public boolean isNegative() {
    return amount.isNegative();
  }

  @Override
  public boolean equals(Object o) {
    if(this == o) return true;
    if(!(o instanceof ProcessedTransaction)) return false;
    var that = (ProcessedTransaction) o;
    return Objects.equals(monzoId, that.monzoId) &&
        Objects.equals(dateTime, that.dateTime) &&
        Objects.equals(amount, that.amount) &&
        Objects.equals(tag, that.tag);
  }

  @Override
  public int hashCode() {
    return Objects.hash(monzoId, dateTime, amount, tag);
  }
}
