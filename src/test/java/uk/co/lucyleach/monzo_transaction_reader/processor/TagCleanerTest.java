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
    var newTag = UNDER_TEST.cleanTag(originalTag, ClientProcessingDetails.builder().build());
    assertEquals(originalTag, newTag);
  }
}