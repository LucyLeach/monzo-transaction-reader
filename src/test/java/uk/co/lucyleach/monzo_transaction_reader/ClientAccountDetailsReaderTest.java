package uk.co.lucyleach.monzo_transaction_reader;

import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * User: Lucy
 * Date: 21/07/2018
 * Time: 21:29
 */
public class ClientAccountDetailsReaderTest {
  private static final ClientAccountDetailsReader UNDER_TEST = new ClientAccountDetailsReader();
  private static final String EXPECTED_CLIENT_ID = "CLIENT_ID";
  private static final String EXPECTED_CLIENT_SECRET = "CLIENT_SECRET";
  private static final String EXPECTED_ACCOUNT_ID = "ACCOUNT_ID";

  @Test
  public void testFullPropertiesFile() throws Exception {
    var testPropertiesFile = propertiesFileByTestName("full");
    var details = UNDER_TEST.read(testPropertiesFile);
    assertNotNull("Should never return null", details);
    assertEquals(EXPECTED_CLIENT_ID, details.getClientId());
    assertEquals(EXPECTED_CLIENT_SECRET, details.getClientSecret());
    assertEquals(EXPECTED_ACCOUNT_ID, details.getAccountId());
    assertEquals(Map.of("Pot name 1", "#Foo", "Pot name 2", "#Bar"), details.getPotsToRecogniseIn());
    assertEquals(Map.of("Pot name 3", "#Foo", "Pot name 4", "#Bar"), details.getPotsToRecogniseOut());
    assertEquals(Map.of("Merchant", "#MerchantTag", "OtherMerchant", "#OtherMerchantTag"), details.getAutoTagMerchants());
    assertEquals(Map.of("123/456", "#Tag1", "789/123", "#Tag2"), details.getAutoTagAccounts());
    assertEquals(Map.of("foo","bar", "foo1", "bar1"), details.getTagsToReplace());
    assertEquals(Map.of("Tag1", "TopTagA", "Tag2", "TopTagA", "Tag3", "TopTagB", "Tag4", "TopTagC", "Tag5", "TopTagC", "Tag6", "TopTagC"), details.getTagCategories());
  }

  @Test
  public void testMissingClientId() throws Exception {
    var missingProperty = "client_id";
    testMissingPropertyError(missingProperty);
  }

  @Test
  public void testMissingClientSecret() throws Exception {
    var missingProperty = "client_secret";
    testMissingPropertyError(missingProperty);
  }

  @Test
  public void testMissingAccountId() throws Exception {
    var missingProperty = "account_id";
    testMissingPropertyError(missingProperty);
  }

  @Test
  public void testEmptyFile() throws Exception {
    var testPropertiesFile = propertiesFileByTestName("empty");
    try {
      UNDER_TEST.read(testPropertiesFile);
      fail("Should have thrown exception");
    } catch(IOException exception) {
      assertTrue("Error message should contain file path", exception.getMessage().contains(testPropertiesFile));
    }
  }

  @Test
  public void testBadlyFormattedMap() throws Exception {
    var testPropertiesFile = propertiesFileByTestName("badly_formatted_map");
    try {
      UNDER_TEST.read(testPropertiesFile);
      fail("Should have thrown exception");
    } catch(IOException exception) {
      assertTrue("Error message should contain file path", exception.getMessage().contains(testPropertiesFile));
    }
  }

  @Test
  public void testOnlyRequiredFields() throws Exception {
    var testPropertiesFile = propertiesFileByTestName("only_req_fields");
    var details = UNDER_TEST.read(testPropertiesFile);
    assertNotNull("Should never return null", details);
    assertEquals(EXPECTED_CLIENT_ID, details.getClientId());
    assertEquals(EXPECTED_CLIENT_SECRET, details.getClientSecret());
    assertEquals(EXPECTED_ACCOUNT_ID, details.getAccountId());
  }

  private void testMissingPropertyError(String missingProperty) throws URISyntaxException {
    var testPropertiesFile = propertiesFileByTestName("missing_" + missingProperty);
    try {
      UNDER_TEST.read(testPropertiesFile);
      fail("Should have thrown exception");
    } catch(IOException exception) {
      assertTrue("Error message should contain missing property name", exception.getMessage().contains(missingProperty));
      assertTrue("Error message should contain file path", exception.getMessage().contains(testPropertiesFile));
    }
  }

  private static String propertiesFileByTestName(String testName) throws URISyntaxException {
    return Paths.get(ClassLoader.getSystemResource(testName + ".properties").toURI()).toString();
  }
}