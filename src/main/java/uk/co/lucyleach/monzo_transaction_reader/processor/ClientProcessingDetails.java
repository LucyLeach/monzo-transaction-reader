package uk.co.lucyleach.monzo_transaction_reader.processor;

import java.util.Map;

/**
 * User: Lucy
 * Date: 28/08/2018
 * Time: 09:56
 */
public class ClientProcessingDetails {
  private final Map<String, String> potsToRecogniseIn;
  private final Map<String, String> potsToRecogniseOut;
  private final Map<String, String> autoTagMerchants;
  private final Map<String, String> autoTagAccounts;

  public ClientProcessingDetails(Map<String, String> potsToRecogniseIn, Map<String, String> potsToRecogniseOut, Map<String, String> autoTagMerchants, Map<String, String> autoTagAccounts) {
    this.potsToRecogniseIn = Map.copyOf(potsToRecogniseIn);
    this.potsToRecogniseOut = Map.copyOf(potsToRecogniseOut);
    this.autoTagMerchants = Map.copyOf(autoTagMerchants);
    this.autoTagAccounts = Map.copyOf(autoTagAccounts);
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
}
