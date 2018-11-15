package uk.co.lucyleach.monzo_transaction_reader;

import com.google.common.base.Splitter;
import com.google.common.collect.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

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
    var autoTagMerchants = readMap("merchants_to_auto_tag", properties, filePath);
    var autoTagAccounts = readMap("accounts_to_auto_tag", properties, filePath);
    var tagsToReplace = readMap("tags_to_replace", properties, filePath);
    var tagCategories = invertMultimap(readMultimap("tag_categories", properties, filePath));
    return new ClientAccountDetails(clientId, clientSecret, accountId, potsToRecogniseIn, potsToRecogniseOut, autoTagMerchants, autoTagAccounts, tagsToReplace, tagCategories);
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

  private Multimap<String, String> readMultimap(String propertyName, Properties properties, String filePath) throws IOException {
    var property = properties.getProperty(propertyName);
    if(property == null) {
      return ArrayListMultimap.create();
    } else {
      try {
        //noinspection UnstableApiUsage
        var keyToCsvString = Splitter.on(";").withKeyValueSeparator("=").split(property);
        //noinspection UnstableApiUsage
        Map<String, Collection<String>> mapToCollection = Maps.transformValues(keyToCsvString, v -> Splitter.on(",").splitToList(v));
        var multimapBuilder = ImmutableMultimap.<String, String>builder();
        mapToCollection.entrySet().stream().forEach(e -> multimapBuilder.putAll(e.getKey(), e.getValue()));
        return multimapBuilder.build();
      } catch(Exception e) {
        throw new IOException("Error interpretting " + propertyName + " as a multimap in file " + filePath + ": " + e.getMessage(), e);
      }
    }
  }

  private Map<String, String> invertMultimap(Multimap<String, String> multimap) {
    var mapBuilder = ImmutableMap.<String, String>builder();
    multimap.asMap().entrySet().stream().forEach(e -> {
      e.getValue().stream().forEach(v -> mapBuilder.put(v, e.getKey()));
    });
    return mapBuilder.build();
  }
}
