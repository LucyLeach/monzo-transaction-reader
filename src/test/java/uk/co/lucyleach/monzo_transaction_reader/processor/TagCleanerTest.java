package uk.co.lucyleach.monzo_transaction_reader.processor;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * User: Lucy
 * Date: 16/09/2018
 * Time: 14:26
 */
public class TagCleanerTest {
  private static TagCleaner UNDER_TEST = new TagCleaner();

  @Test
  public void testNoChange() {
    var originalTag = "PlainOldTag";
    var cleanedTag = UNDER_TEST.cleanTag(originalTag, ClientProcessingDetails.builder().build());
    assertEquals(originalTag, cleanedTag);
  }

  @Test
  public void testLowerToUpperCase() {
    var originalTag = "startsLowerCase";
    var cleanedTag = UNDER_TEST.cleanTag(originalTag, ClientProcessingDetails.builder().build());
    assertEquals("StartsLowerCase", cleanedTag);
  }

  @Test
  public void testReplacementInDetails() {
    var originalTag = "OriginalTag";
    var newTag = "NewTag";
    var cleanedTag = UNDER_TEST.cleanTag(originalTag, ClientProcessingDetails.builder().addTagToReplace(originalTag, newTag).build());
    assertEquals(newTag, cleanedTag);
  }

  @Test
  public void testReplacementAfterLowerCaseCorrection() {
    var originalTag = "lowerCase";
    var tagToReplace = "LowerCase";
    var newTag = "ReplacedTag";
    var cleanedTag = UNDER_TEST.cleanTag(originalTag, ClientProcessingDetails.builder().addTagToReplace(tagToReplace, newTag).build());
    assertEquals(newTag, cleanedTag);
  }
}