package uk.co.lucyleach.monzo_transaction_reader.processor;

import java.util.Objects;

public class ReasonIgnored_Pot implements ReasonIgnored {
  private final String potName;

  public ReasonIgnored_Pot(String potName) {
    this.potName = potName;
  }

  @Override
  public String toReadableString() {
    return "Unrecognised Pot: " + potName;
  }

  @Override
  public boolean equals(Object o) {
    if(this == o) return true;
    if(o == null || getClass() != o.getClass()) return false;
    ReasonIgnored_Pot that = (ReasonIgnored_Pot) o;
    return Objects.equals(potName, that.potName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(potName);
  }
}
