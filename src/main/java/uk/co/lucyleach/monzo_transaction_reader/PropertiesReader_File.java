package uk.co.lucyleach.monzo_transaction_reader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * User: Lucy
 * Date: 21/07/2018
 * Time: 21:05
 */
public class PropertiesReader_File
{
  public Properties readProperties(String filePath) throws IOException
  {
    try (InputStream stream = Files.newInputStream(Paths.get(filePath)))
    {
      Properties properties = new Properties();
      properties.load(stream);
      return properties;
    }
  }
}
