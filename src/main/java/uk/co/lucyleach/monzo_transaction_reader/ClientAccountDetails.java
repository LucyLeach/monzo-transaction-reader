package uk.co.lucyleach.monzo_transaction_reader;

import java.util.Map;

/**
 * User: Lucy
 * Date: 28/07/2018
 * Time: 21:17
 */
class ClientAccountDetails {
  private final String clientId;
  private final String clientSecret;
  private final String accountId;
  private final Map<String, String> potsToRecogniseIn;
  private final Map<String, String> potsToRecogniseOut;
  private final Map<String, String> autoTagMerchants;
  private final Map<String, String> autoTagAccounts;

  ClientAccountDetails(String clientId, String clientSecret, String accountId, Map<String, String> potsToRecogniseIn,
                       Map<String, String> potsToRecogniseOut, Map<String, String> autoTagMerchants, Map<String, String> autoTagAccounts) {
    this.clientId = clientId;
    this.clientSecret = clientSecret;
    this.accountId = accountId;
    this.potsToRecogniseIn = potsToRecogniseIn;
    this.potsToRecogniseOut = potsToRecogniseOut;
    this.autoTagMerchants = autoTagMerchants;
    this.autoTagAccounts = autoTagAccounts;
  }

  String getClientId() {
    return clientId;
  }

  String getClientSecret() {
    return clientSecret;
  }

  String getAccountId() {
    return accountId;
  }

  Map<String, String> getPotsToRecogniseIn() {
    return potsToRecogniseIn;
  }

  Map<String, String> getPotsToRecogniseOut() {
    return potsToRecogniseOut;
  }

  Map<String, String> getAutoTagMerchants() {
    return autoTagMerchants;
  }

  Map<String, String> getAutoTagAccounts() {
    return autoTagAccounts;
  }
}
