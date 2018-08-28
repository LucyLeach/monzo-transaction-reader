package uk.co.lucyleach.monzo_transaction_reader;

import com.google.common.base.Splitter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;

/**
 * User: Lucy
 * Date: 21/07/2018
 * Time: 21:05
 */
class ClientAccountDetailsReader {
  ClientAccountDetails read(String filePath) throws IOException {
    var properties = new Properties();
    try(var stream = Files.newInputStream(Paths.get(filePath))) {
      properties.load(stream);
    }

    var clientId = readNonNullProperty("client_id", properties, filePath);
    var clientSecret = readNonNullProperty("client_secret", properties, filePath);
    var accountId = readNonNullProperty("account_id", properties, filePath);
    var potsToRecogniseIn = readMap("pots_to_recognise_in", properties, filePath);
    var potsToRecogniseOut = readMap("pots_to_recognise_out", properties, filePath);
    return new ClientAccountDetails(clientId, clientSecret, accountId, potsToRecogniseIn, potsToRecogniseOut);
  }

  private String readNonNullProperty(String propertyName, Properties properties, String filePath) throws IOException {
    var property = properties.getProperty(propertyName);
    if(property == null)
      throw new IOException("Client ID not found in properties file: " + filePath);
    else
      return property;
  }

  private Map<String, String> readMap(String propertyName, Properties properties, String filePath) throws IOException {
    var property = properties.getProperty(propertyName);
    if(property == null) {
      return Map.of();
    } else {
      try {
        //noinspection UnstableApiUsage
        return Splitter.on(",").withKeyValueSeparator("=").split(property);
      } catch(Exception e) {
        throw new IOException("Error interpretting " + propertyName + " as a map in file " + filePath + ": " + e.getMessage(), e);
      }
    }
  }
}
