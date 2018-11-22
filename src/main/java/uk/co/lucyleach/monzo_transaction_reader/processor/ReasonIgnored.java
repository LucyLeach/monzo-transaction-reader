package uk.co.lucyleach.monzo_transaction_reader.processor;

/**
 * User: Lucy
 * Date: 08/09/2018
 * Time: 09:50
 */
public enum ReasonIgnored {
  IGNORE_TAG("Tagged with #Ignore"),
  NON_GBP("Not in GBP"),
  DECLINED("Declined transaction"),
  ZERO_TRANSACTION("Zero transaction"),
  UNCONFIGURED_POT("Unconfigured pot");

  private final String humanString;

  ReasonIgnored(String humanString) {
    this.humanString = humanString;
  }

  @Override
  public String toString() {
    return humanString;
  }
}
