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

  public ClientProcessingDetails(Map<String, String> potsToRecogniseIn, Map<String, String> potsToRecogniseOut) {
    this.potsToRecogniseIn = Map.copyOf(potsToRecogniseIn);
    this.potsToRecogniseOut = Map.copyOf(potsToRecogniseOut);
  }

  public Map<String, String> getPotsToRecogniseIn() {
    return potsToRecogniseIn;
  }

  public Map<String, String> getPotsToRecogniseOut() {
    return potsToRecogniseOut;
  }
}
