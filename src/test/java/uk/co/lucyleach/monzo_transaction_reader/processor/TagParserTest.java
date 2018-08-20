package uk.co.lucyleach.monzo_transaction_reader.processor;

import org.junit.Test;
import uk.co.lucyleach.monzo_transaction_reader.processor.TagParser.ParsingException;

import static org.junit.Assert.*;

/**
 * User: Lucy
 * Date: 20/08/2018
 * Time: 11:52
 */
public class TagParserTest {
  private static final TagParser UNDER_TEST = new TagParser();

  @Test
  public void testSimpleTag() throws ParsingException {
    var tag = "SimpleTag";
    Integer totalAmount = -100;
    var resultMap = UNDER_TEST.parseTags("#" + tag, totalAmount);
    assertNotNull(resultMap);
    assertEquals(resultMap.size(), 1);
    assertTrue(resultMap.containsKey(tag));
    assertEquals(resultMap.get(tag), totalAmount);
  }

  @Test
  public void testSingleTagWithAmount() throws ParsingException {
    var resultMap = UNDER_TEST.parseTags("1.7 #Tag", -170);
    assertNotNull(resultMap);
    assertEquals(resultMap.size(), 1);
    assertTrue(resultMap.containsKey("Tag"));
    assertEquals(resultMap.get("Tag"), Integer.valueOf(-170));
  }

  @Test(expected = ParsingException.class)
  public void testMissingHash() throws ParsingException {
    UNDER_TEST.parseTags("NoHash", -100);
  }

  @Test
  public void testMultiTags() throws ParsingException {
    var resultMap = UNDER_TEST.parseTags("1.7 #FirstTag; 2.8 #SecondTag", -450);
    assertNotNull(resultMap);
    assertEquals(resultMap.size(), 2);
    assertTrue(resultMap.containsKey("FirstTag"));
    assertEquals(resultMap.get("FirstTag"), Integer.valueOf(-170));
    assertTrue(resultMap.containsKey("SecondTag"));
    assertEquals(resultMap.get("SecondTag"), Integer.valueOf(-280));
  }

  @Test
  public void testMultiTagsWithRest() throws ParsingException {
    var resultMap = UNDER_TEST.parseTags("1.7 #FirstTag; rest #SecondTag", -450);
    assertNotNull(resultMap);
    assertEquals(resultMap.size(), 2);
    assertTrue(resultMap.containsKey("FirstTag"));
    assertEquals(resultMap.get("FirstTag"), Integer.valueOf(-170));
    assertTrue(resultMap.containsKey("SecondTag"));
    assertEquals(resultMap.get("SecondTag"), Integer.valueOf(-280));
  }

  @Test(expected = ParsingException.class)
  public void testMultiTagsDontAddUp() throws ParsingException {
    UNDER_TEST.parseTags("1.7 #FirstTag; 2.8 #SecondTag", -300);
  }

  @Test(expected = ParsingException.class)
  public void testMultiTagMissingHash() throws ParsingException {
    UNDER_TEST.parseTags("1.7 #FirstTag; 2.8 MissingHash", -450);
  }

  @Test(expected = ParsingException.class)
  public void testDuplicateTags() throws ParsingException {
    UNDER_TEST.parseTags("1.7 #FirstTag; 2.8 #FirstTag", -450);
  }

  @Test(expected = ParsingException.class)
  public void testCommaSeparated() throws ParsingException {
    UNDER_TEST.parseTags("1.7 #FirstTag, 2.8 #SecondTag", -450);
  }

  @Test(expected = ParsingException.class)
  public void testDoubleTag() throws ParsingException {
    UNDER_TEST.parseTags("1.7 #FirstTag; 2.8 #SecondTag #ThirdTag", -450);
  }

  @Test(expected = ParsingException.class)
  public void testMultiTagsMissingAmount() throws ParsingException {
    UNDER_TEST.parseTags("1.7 #FirstTag; #SecondTag", -170);
  }

  @Test(expected = ParsingException.class)
  public void testMoreThanOneRest() throws ParsingException {
    UNDER_TEST.parseTags("1.7 #FirstTag, rest #SecondTag; rest #ThirdTag", -450);
  }
}