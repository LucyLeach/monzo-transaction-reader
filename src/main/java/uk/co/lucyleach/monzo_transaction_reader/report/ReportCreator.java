package uk.co.lucyleach.monzo_transaction_reader.report;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import uk.co.lucyleach.monzo_transaction_reader.monzo_model.Transaction;
import uk.co.lucyleach.monzo_transaction_reader.output_model.Money;
import uk.co.lucyleach.monzo_transaction_reader.output_model.ProcessedTransaction;
import uk.co.lucyleach.monzo_transaction_reader.processor.ReasonIgnored;
import uk.co.lucyleach.monzo_transaction_reader.processor.TransactionProcessorResult;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

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

    var amountIn = sumTransactionsWithFilter(processedTransactions, ProcessedTransaction::isPositive);
    var amountOut = sumTransactionsWithFilter(processedTransactions, ProcessedTransaction::isNegative);

    var dateToNegativeTransactionMap = processedTransactions.stream().filter(ProcessedTransaction::isNegative).collect(Collectors.groupingBy(t -> t.getDateTime().toLocalDate()));
    var dateToExpenditureMap = Maps.transformValues(dateToNegativeTransactionMap, ReportCreator::sumTransactions);
    var sortedDateToExpenditureMap = new TreeMap<>(dateToExpenditureMap);

    var ignoredTransactionToReasonMap = result.getIgnoredTransactions();
    var reasonToTransactionMap = invertMap(ignoredTransactionToReasonMap);
    var ignoredTransactionsReports = reasonToTransactionMap.entrySet().stream()
        .map(ReportCreator::createIgnoredTransactionReport)
        .sorted(comparing(IgnoredTransactionsReport::getReasonIgnored))
        .collect(toList());

    return new TransactionReport(amountIn, amountOut, tagReports, sortedDateToExpenditureMap, ignoredTransactionsReports);
  }

  private static Map<ReasonIgnored, Collection<Transaction>> invertMap(Map<Transaction, ReasonIgnored> originalMap) {
    Multimap<ReasonIgnored, Transaction> multimap = ArrayListMultimap.create();
    originalMap.forEach((key, value) -> multimap.put(value, key));
    return multimap.asMap();
  }

  private static IgnoredTransactionsReport createIgnoredTransactionReport(Map.Entry<ReasonIgnored, Collection<Transaction>> entry) {
    var reason = entry.getKey();
    var transactions = entry.getValue();

    var sortedTransactions = transactions.stream().sorted(comparing(Transaction::getCreated)).collect(toList());

    var gbp = "GBP";
    var amountIn = transactions.stream().filter(t -> gbp.equals(t.getCurrency())).filter(t -> t.getAmount() > 0).mapToInt(Transaction::getAmount).sum();
    var amountOut = transactions.stream().filter(t -> gbp.equals(t.getCurrency())).filter(t -> t.getAmount() < 0).mapToInt(Transaction::getAmount).sum();

    return new IgnoredTransactionsReport(reason, new Money(amountIn, gbp), new Money(amountOut, gbp), sortedTransactions);
  }

  private static TagLevelReport createTagLevelReport(Map.Entry<String, List<ProcessedTransaction>> tagMapEntry) {
    var tag = tagMapEntry.getKey();
    var transactions = tagMapEntry.getValue();

    var sortedTransactions = transactions.stream().sorted(comparing(ProcessedTransaction::getDateTime)).collect(toList());

    var amountIn = sumTransactionsWithFilter(transactions, ProcessedTransaction::isPositive);
    var amountOut = sumTransactionsWithFilter(transactions, ProcessedTransaction::isNegative);

    return new TagLevelReport(tag, amountIn, amountOut, sortedTransactions);
  }

  private static Money sumTransactions(Collection<ProcessedTransaction> transactions) {
    return  sumTransactionsWithFilter(transactions, always -> true);
  }

  private static Money sumTransactionsWithFilter(Collection<ProcessedTransaction> transactions, Predicate<ProcessedTransaction> predicate) {
    var currency = checkAndGetSingleCurrency(transactions);
    var amount = transactions.stream().filter(predicate).map(ProcessedTransaction::getAmount).mapToInt(Money::getAmountInPence).sum();
    return new Money(amount, currency);
  }

  private static String checkAndGetSingleCurrency(Collection<ProcessedTransaction> transactions) {
    checkArgument(!transactions.isEmpty());
    var currency = transactions.iterator().next().getAmount().getCurrency();
    checkArgument(transactions.stream().allMatch(t -> currency.equals(t.getAmount().getCurrency())));
    return currency;
  }
}
