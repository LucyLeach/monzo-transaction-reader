package uk.co.lucyleach.monzo_transaction_reader.monzo_model;

import com.google.api.client.util.Key;

import java.util.List;

/**
 * User: Lucy
 * Date: 02/03/2019
 * Time: 14:18
 */
public class PotList {
  @Key
  private List<Pot> pots;

  public PotList() {
  }

  public PotList(Pot... pots) {
    this.pots = List.of(pots);
  }

  public List<Pot> getPots() {
    return pots == null ? List.of() : List.copyOf(pots);
  }
}
