package uk.co.lucyleach.monzo_transaction_reader;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * User: Lucy
 * Date: 02/07/2018
 * Time: 20:17
 */
public class HelloWorldTest
{
  @Test
  public void produceMessage()
  {
    assertEquals("Hello World", HelloWorld.produceMessage());
  }
}