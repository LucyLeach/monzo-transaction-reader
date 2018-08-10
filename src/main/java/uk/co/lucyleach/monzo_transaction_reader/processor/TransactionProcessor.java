package uk.co.lucyleach.monzo_transaction_reader.processor;

import uk.co.lucyleach.monzo_transaction_reader.monzo_model.Transaction;
import uk.co.lucyleach.monzo_transaction_reader.monzo_model.TransactionList;
import uk.co.lucyleach.monzo_transaction_reader.output_model.Money;
import uk.co.lucyleach.monzo_transaction_reader.output_model.ProcessedTransaction;
import uk.co.lucyleach.monzo_transaction_reader.output_model.SaleTransaction;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

/**
 * User: Lucy
 * Date: 05/08/2018
 * Time: 20:28
 */
public class TransactionProcessor {
  public TransactionProcessorResult process(TransactionList transactions) {
    var results = transactions.getTransactions().stream()
        .map(this::process)
        .collect(toSet());
    return new TransactionProcessorResult(results);
  }

  private ProcessorResult process(Transaction original) {
    try {
      return new ProcessorResult(original, processWithExceptions(original));
    } catch(ProcessorException e) {
      return new ProcessorResult(original, e);
    }
  }

  private Set<ProcessedTransaction> processWithExceptions(Transaction original) throws ProcessorException {
    if(original.getMerchant() != null) {
      return Set.of(new SaleTransaction(original.getId(), convertDateTime(original.getCreated()), new Money(original.getAmount(), original.getCurrency()),
          original.getMerchant().getName(), original.getNotes())); //TODO incorrect tag parsing!
    } else {
      throw new ProcessorException("Only implemented sale transactions");
    }
  }

  private static ZonedDateTime convertDateTime(String dateTimeString) {
    return Instant.parse(dateTimeString).atZone(ZoneId.of("UTC"));
  }
}
