package uk.co.lucyleach.monzo_transaction_reader.processor;

import uk.co.lucyleach.monzo_transaction_reader.monzo_model.Transaction;

/**
 * User: Lucy
 * Date: 29/07/2018
 * Time: 21:28
 */
class UnsuccessfulProcessorResult extends Pair<Transaction, String>
{
  protected UnsuccessfulProcessorResult(Transaction transaction, String message)
  {
    super(transaction, message);
  }

  public Transaction getOriginalTransaction()
  {
    return getA();
  }

  public String getMessage()
  {
    return getB();
  }
}
