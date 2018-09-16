package uk.co.lucyleach.monzo_transaction_reader.processor;

/**
 * User: Lucy
 * Date: 16/09/2018
 * Time: 14:21
 */
public class TagCleaner {
  public String cleanTag(String originalTag, ClientProcessingDetails clientDetails) {
    char firstCharacter = originalTag.charAt(0);
    if(Character.isLowerCase(firstCharacter)) {
      return originalTag.replaceFirst(String.valueOf(firstCharacter), String.valueOf(Character.toUpperCase(firstCharacter)));
    } else {
      return originalTag;
    }
  }
}
