package uk.co.lucyleach.monzo_transaction_reader.processor;

/**
 * User: Lucy
 * Date: 16/09/2018
 * Time: 14:21
 */
public class TagCleaner {
  public String cleanTag(String originalTag, ClientProcessingDetails clientDetails) {
    var changingTag = replaceInitialLowerCaseCharacter(originalTag);
    changingTag = replaceFromClientDetails(changingTag, clientDetails);
    return changingTag;
  }

  private String replaceInitialLowerCaseCharacter(String originalTag) {
    var firstCharacter = originalTag.charAt(0);
    if(Character.isLowerCase(firstCharacter)) {
      return originalTag.replaceFirst(String.valueOf(firstCharacter), String.valueOf(Character.toUpperCase(firstCharacter)));
    } else {
      return originalTag;
    }
  }

  private String replaceFromClientDetails(String originalTag, ClientProcessingDetails clientDetails) {
    var tagsToReplace = clientDetails.getTagsToReplace();
    return tagsToReplace.getOrDefault(originalTag, originalTag);
  }
}
