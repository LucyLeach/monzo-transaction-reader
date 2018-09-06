package uk.co.lucyleach.monzo_transaction_reader.processor;

/**
 * User: Lucy
 * Date: 27/08/2018
 * Time: 13:24
 */
class ParsingException extends Exception {
  ParsingException(String message) {
    super(message);
  }
}
