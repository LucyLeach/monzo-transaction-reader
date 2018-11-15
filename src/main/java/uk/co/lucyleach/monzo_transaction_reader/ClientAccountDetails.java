package uk.co.lucyleach.monzo_transaction_reader;

import java.util.Map;

/**
 * User: Lucy
 * Date: 28/07/2018
 * Time: 21:17
 */
public class ClientAccountDetails {
  private final String clientId;
  private final String clientSecret;
  private final String accountId;
  private final Map<String, String> potsToRecogniseIn;
  private final Map<String, String> potsToRecogniseOut;
  private final Map<String, String> autoTagMerchants;
  private final Map<String, String> autoTagAccounts;
  private final Map<String, String> tagsToReplace;
  private final Map<String, String> tagCategories;

  ClientAccountDetails(String clientId, String clientSecret, String accountId, Map<String, String> potsToRecogniseIn,
                       Map<String, String> potsToRecogniseOut, Map<String, String> autoTagMerchants, Map<String, String> autoTagAccounts,
                       Map<String, String> tagsToReplace, Map<String, String> tagCategories) {
    this.clientId = clientId;
    this.clientSecret = clientSecret;
    this.accountId = accountId;
    this.potsToRecogniseIn = potsToRecogniseIn;
    this.potsToRecogniseOut = potsToRecogniseOut;
    this.autoTagMerchants = autoTagMerchants;
    this.autoTagAccounts = autoTagAccounts;
    this.tagsToReplace = tagsToReplace;
    this.tagCategories = tagCategories;
  }

  public String getClientId() {
    return clientId;
  }

  public String getClientSecret() {
    return clientSecret;
  }

  public String getAccountId() {
    return accountId;
  }

  public Map<String, String> getPotsToRecogniseIn() {
    return potsToRecogniseIn;
  }

  public Map<String, String> getPotsToRecogniseOut() {
    return potsToRecogniseOut;
  }

  public Map<String, String> getAutoTagMerchants() {
    return autoTagMerchants;
  }

  public Map<String, String> getAutoTagAccounts() {
    return autoTagAccounts;
  }

  public Map<String, String> getTagsToReplace() {
    return tagsToReplace;
  }

  public Map<String, String> getTagCategories() {
    return tagCategories;
  }
}
