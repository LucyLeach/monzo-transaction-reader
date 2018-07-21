package uk.co.lucyleach.monzo_transaction_reader;

import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Properties;

import static org.junit.Assert.*;

/**
 * User: Lucy
 * Date: 21/07/2018
 * Time: 21:29
 */
public class PropertiesReader_FileTest
{
  @Test
  public void testReadingPropertiesFile() throws URISyntaxException
  {
    String testPropertiesFile = Paths.get(ClassLoader.getSystemResource("test.properties").toURI()).toString();
    PropertiesReader_File propertiesReader = new PropertiesReader_File();
    Properties properties = null;
    try
    {
      properties = propertiesReader.readProperties(testPropertiesFile);
    } catch (IOException e)
    {
      fail("Encountered IO exception: " + e.getMessage());
    }
    assertNotNull("Properties should never be null", properties);
    assertEquals("foo", properties.getProperty("property.one"));
    assertEquals("bar", properties.getProperty("property.two"));
    assertNull(properties.getProperty("property.three"));
  }
}