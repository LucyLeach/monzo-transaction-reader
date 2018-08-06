package uk.co.lucyleach.monzo_transaction_reader;

/**
 * User: Lucy
 * Date: 28/07/2018
 * Time: 21:17
 */
class ClientAccountDetails {
  private final String clientId;
  private final String clientSecret;
  private final String accountId;

  ClientAccountDetails(String clientId, String clientSecret, String accountId) {
    this.clientId = clientId;
    this.clientSecret = clientSecret;
    this.accountId = accountId;
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
}
