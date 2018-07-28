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
class ClientAccountDetailsReader
{
  ClientAccountDetails read(String filePath) throws IOException
  {
    Properties properties = new Properties();
    try (InputStream stream = Files.newInputStream(Paths.get(filePath)))
    {
      properties.load(stream);
    }

    String clientId = readNonNullProperty("client_id", properties, filePath);
    String clientSecret = readNonNullProperty("client_secret", properties, filePath);
    String accountId = readNonNullProperty("account_id", properties, filePath);
    return new ClientAccountDetails(clientId, clientSecret, accountId);
  }

  private String readNonNullProperty(String propertyName, Properties properties, String filePath) throws IOException
  {
    String property = properties.getProperty(propertyName);
    if (property == null)
      throw new IOException("Client ID not found in properties file: " + filePath);
    else
      return property;
  }
}
