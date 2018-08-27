package uk.co.lucyleach.monzo_transaction_reader.utils;

/**
 * User: Lucy
 * Date: 29/07/2018
 * Time: 21:21
 */
public class Pair<A, B> {
  private final A a;
  private final B b;

  public Pair(A a, B b) {
    this.a = a;
    this.b = b;
  }

  public A getA() {
    return a;
  }

  public B getB() {
    return b;
  }
}
