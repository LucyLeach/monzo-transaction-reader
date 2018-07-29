package uk.co.lucyleach.monzo_transaction_reader.processor;

/**
 * User: Lucy
 * Date: 29/07/2018
 * Time: 21:21
 */
abstract class Pair<A,B>
{
  private final A a;
  private final B b;

  Pair(A a, B b)
  {
    this.a = a;
    this.b = b;
  }

  A getA()
  {
    return a;
  }

  B getB()
  {
    return b;
  }
}
