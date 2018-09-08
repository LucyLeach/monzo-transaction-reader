package uk.co.lucyleach.monzo_transaction_reader;

import uk.co.lucyleach.monzo_transaction_reader.output_model.Money;
import uk.co.lucyleach.monzo_transaction_reader.output_model.ProcessedTransaction;
import uk.co.lucyleach.monzo_transaction_reader.processor.TransactionProcessorResult;
import uk.co.lucyleach.monzo_transaction_reader.utils.Pair;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.*;

/**
 * User: Lucy
 * Date: 07/09/2018
 * Time: 21:39
 */
public class ReportCreator {
  public TransactionReport create(TransactionProcessorResult result) {
    var processedTransactions = result.getSuccessfulResults().values().stream().flatMap(Collection::stream).collect(toSet());

    var tagMap = processedTransactions.stream().collect(Collectors.groupingBy(ProcessedTransaction::getTag));
    var tagReports = tagMap.entrySet().stream()
        .map(ReportCreator::createTagLevelReport)
        .sorted(comparing(TagLevelReport::getTag))
        .collect(toList());

    var amountIn = sumTransactionsWithFilter(processedTransactions, t -> t.getAmount().isPositive());
    var amountOut = sumTransactionsWithFilter(processedTransactions, t -> t.getAmount().isNegative());

    var dateToNegativeTransactionMap = processedTransactions.stream().filter(t -> t.getAmount().isNegative()).collect(Collectors.groupingBy(t -> t.getDateTime().toLocalDate()));
    var dateToExpenditureMap = dateToNegativeTransactionMap.entrySet().stream()
        .map(e -> new Pair<>(e.getKey(), sumTransactionsWithFilter(e.getValue(), i -> true)))
        .collect(toMap(Pair::getA, Pair::getB));
    var sortedDateToExpenditureMap = new TreeMap<>(dateToExpenditureMap);

    return new TransactionReport(amountIn, amountOut, tagReports, sortedDateToExpenditureMap);
  }

  private static TagLevelReport createTagLevelReport(Map.Entry<String, ? extends Collection<? extends ProcessedTransaction>> tagMapEntry) {
    var tag = tagMapEntry.getKey();
    var transactions = tagMapEntry.getValue();

    var sortedTransactions = transactions.stream().sorted(comparing(ProcessedTransaction::getDateTime)).collect(toList());

    var amountIn = sumTransactionsWithFilter(transactions, t -> t.getAmount().isPositive());
    var amountOut = sumTransactionsWithFilter(transactions, t -> t.getAmount().isNegative());

    return new TagLevelReport(tag, amountIn, amountOut, sortedTransactions);
  }

  private static Money sumTransactionsWithFilter(Collection<? extends ProcessedTransaction> transactions, Predicate<ProcessedTransaction> predicate) {
    var currency = checkAndGetSingleCurrency(transactions);
    var amount = transactions.stream().filter(predicate).map(ProcessedTransaction::getAmount).mapToInt(Money::getAmountInPence).sum();
    return new Money(amount, currency);
  }

  private static String checkAndGetSingleCurrency(Collection<? extends ProcessedTransaction> transactions) {
    checkArgument(!transactions.isEmpty());
    var currency = transactions.iterator().next().getAmount().getCurrency();
    checkArgument(transactions.stream().allMatch(t -> currency.equals(t.getAmount().getCurrency())));
    return currency;
  }
}
