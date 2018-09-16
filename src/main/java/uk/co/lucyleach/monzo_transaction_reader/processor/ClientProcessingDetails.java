package uk.co.lucyleach.monzo_transaction_reader.processor;

import java.util.HashMap;
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
  private final Map<String, String> tagsToReplace;

  private ClientProcessingDetails(Map<String, String> potsToRecogniseIn, Map<String, String> potsToRecogniseOut, Map<String, String> autoTagMerchants,
                                  Map<String, String> autoTagAccounts, Map<String, String> tagsToReplace) {
    this.potsToRecogniseIn = Map.copyOf(potsToRecogniseIn);
    this.potsToRecogniseOut = Map.copyOf(potsToRecogniseOut);
    this.autoTagMerchants = Map.copyOf(autoTagMerchants);
    this.autoTagAccounts = Map.copyOf(autoTagAccounts);
    this.tagsToReplace = Map.copyOf(tagsToReplace);
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

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private final Map<String, String> potsToRecogniseIn;
    private final Map<String, String> potsToRecogniseOut;
    private final Map<String, String> autoTagMerchants;
    private final Map<String, String> autoTagAccounts;
    private final Map<String, String> tagsToReplace;

    public Builder() {
      potsToRecogniseIn = new HashMap<>();
      potsToRecogniseOut = new HashMap<>();
      autoTagMerchants = new HashMap<>();
      autoTagAccounts = new HashMap<>();
      tagsToReplace = new HashMap<>();
    }

    public Builder addPotsToRecogniseIn(Map<String, String> potsToRecogniseIn) {
      this.potsToRecogniseIn.putAll(potsToRecogniseIn);
      return this;
    }

    public Builder addPotToRecogniseIn(String potId, String tag) {
      this.potsToRecogniseIn.put(potId, tag);
      return this;
    }

    public Builder addPotsToRecogniseOut(Map<String, String> potsToRecogniseOut) {
      this.potsToRecogniseOut.putAll(potsToRecogniseOut);
      return this;
    }

    public Builder addPotToRecogniseOut(String potId, String tag) {
      this.potsToRecogniseOut.put(potId, tag);
      return this;
    }

    public Builder addAutoTagMerchants(Map<String, String> autoTagMerchants) {
      this.autoTagMerchants.putAll(autoTagMerchants);
      return this;
    }

    public Builder addAutoTagMerchant(String merchantName, String tag) {
      this.autoTagMerchants.put(merchantName, tag);
      return this;
    }

    public Builder addAutoTagAccounts(Map<String, String> autoTagAccounts) {
      this.autoTagAccounts.putAll(autoTagAccounts);
      return this;
    }

    public Builder addAutoTagAccount(String accountId, String tag) {
      this.autoTagAccounts.put(accountId, tag);
      return this;
    }

    public Builder addTagsToReplace(Map<String, String> tagsToReplace) {
      this.tagsToReplace.putAll(tagsToReplace);
      return this;
    }

    public Builder addTagToReplace(String originalTag, String replacementTag) {
      this.tagsToReplace.put(originalTag, replacementTag);
      return this;
    }

    public ClientProcessingDetails build() {
      return new ClientProcessingDetails(potsToRecogniseIn, potsToRecogniseOut, autoTagMerchants, autoTagAccounts, tagsToReplace);
    }
  }
}
