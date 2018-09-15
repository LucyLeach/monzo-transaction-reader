package uk.co.lucyleach.monzo_transaction_reader.monzo_model;

import com.google.api.client.json.JsonString;
import com.google.api.client.util.Key;

/**
 * User: Lucy
 * Date: 22/07/2018
 * Time: 20:58
 */
@SuppressWarnings("WeakerAccess") //Class must be public to be used by Jackson
public class Counterparty {
  @Key("account_number")
  @JsonString
  private Integer accountNumber;

  @Key("sort_code")
  @JsonString
  private Integer sortCode;

  public Counterparty() {
  }

  public Counterparty(Integer accountNumber, Integer sortCode) {
    this.accountNumber = accountNumber;
    this.sortCode = sortCode;
  }

  public Integer getAccountNumber() {
    return accountNumber;
  }

  public Integer getSortCode() {
    return sortCode;
  }

  public boolean isNonEmpty() {
    return accountNumber != null && sortCode != null;
  }

  public String getAccountId() {
    return isNonEmpty() ? accountNumber + "/" + sortCode : "no account";
  }

  @Override
  public String toString() {
    return "Counterparty{" +
        "accountNumber=" + accountNumber +
        ", sortCode=" + sortCode +
        '}';
  }
}
