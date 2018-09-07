package uk.co.lucyleach.monzo_transaction_reader.output_model;

import java.util.Objects;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * User: Lucy
 * Date: 29/07/2018
 * Time: 21:03
 */
public class Money {
  private final int amountInPence;
  private final String currency;

  public Money(int amountInPence, String currency) {
    this.amountInPence = amountInPence;
    this.currency = currency;
  }

  public int getAmountInPence() {
    return amountInPence;
  }

  public String getCurrency() {
    return currency;
  }

  public Money add(Money otherMoney) {
    checkArgument(otherMoney.currency.equals(currency));
    return new Money(amountInPence + otherMoney.amountInPence, currency);
  }

  public boolean isPositive() {
    return amountInPence > 0;
  }

  public boolean isNegative() {
    return amountInPence < 0;
  }

  @Override
  public String toString() {
    return amountInPence + " " + currency;
  }

  @Override
  public boolean equals(Object o) {
    if(this == o) return true;
    if(o == null || getClass() != o.getClass()) return false;
    var money = (Money) o;
    return amountInPence == money.amountInPence &&
        Objects.equals(currency, money.currency);
  }

  @Override
  public int hashCode() {
    return Objects.hash(amountInPence, currency);
  }
}
