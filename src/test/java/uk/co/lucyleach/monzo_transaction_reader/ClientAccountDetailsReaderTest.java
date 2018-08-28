package uk.co.lucyleach.monzo_transaction_reader;

import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;

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