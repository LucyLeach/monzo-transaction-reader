package uk.co.lucyleach.monzo_transaction_reader.report;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import uk.co.lucyleach.monzo_transaction_reader.monzo_model.Transaction;
import uk.co.lucyleach.monzo_transaction_reader.output_model.Money;
import uk.co.lucyleach.monzo_transaction_reader.output_model.ProcessedTransaction;
import uk.co.lucyleach.monzo_transaction_reader.processor.ReasonIgnored;
import uk.co.lucyleach.monzo_transaction_reader.processor.TransactionProcessorResult;
import uk.co.lucyleach.monzo_transaction_reader.utils.Pair;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
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
  private static final String INCOME_TAG = "income";

  public TransactionReport create(TransactionProcessorResult result, Map<String, String> tagCategories) {
    var monthlyReportsByFirstOfMonth = splitAndCreateReports(result, tagCategories);
    var sortedTagsWithCategories = getAllTagsSortedWithCategories(monthlyReportsByFirstOfMonth.values());
    var categoryReports = getCategoryReports(monthlyReportsByFirstOfMonth.values());
    var ignoredTransactionReports = createIgnoredTransactionsReports(result);
    return new TransactionReport(monthlyReportsByFirstOfMonth, sortedTagsWithCategories, categoryReports, ignoredTransactionReports);
  }

  private List<CategoryReport> getCategoryReports(Collection<MonthlyTransactionReport> monthlyReports) {
    var allCategories = monthlyReports.stream()
        .flatMap(sr -> sr.getTagReports().stream())
        .map(TagLevelReport::getTagCategory)
        .distinct()
        .sorted()
        .collect(toList());
    var categoryReports = new ArrayList<CategoryReport>();
    for(var category : allCategories) {
      var amountBySplit = new ArrayList<Double>();
      for(var monthlyReport : monthlyReports) {
        var amount = monthlyReport.getTagReports().stream()
            .filter(tr -> category.equals(tr.getTagCategory()))
            .mapToDouble(tr -> tr.getTotalAmount().getAmountInPounds().doubleValue())
            .sum();
        amountBySplit.add(amount);
      }
      categoryReports.add(new CategoryReport(category, amountBySplit));
    }
    return categoryReports;
  }

  private List<Pair<String, String>> getAllTagsSortedWithCategories(Collection<MonthlyTransactionReport> monthlyReports) {
    return monthlyReports.stream()
        .map(MonthlyTransactionReport::getTagReports)
        .flatMap(Collection::stream)
        .map(r -> new Pair<>(r.getTag(), r.getTagCategory()))
        .distinct()
        .sorted(Comparator.comparing(Pair::getA))
        .collect(toList());
  }

  private Map<LocalDate, MonthlyTransactionReport> splitAndCreateReports(TransactionProcessorResult processorResult, Map<String, String> tagCategories) {
    if(processorResult.getSuccessfulResults().isEmpty()) {
      return Map.of();
    }

    var allProcessedTransactions = processorResult.getSuccessfulResults().values().stream().flatMap(Collection::stream).collect(toSet());
    var monthlyTransMap = allProcessedTransactions.stream().collect(Collectors.groupingBy(getFirstOfMonth()));
    var monthlyReportMap = Maps.transformValues(monthlyTransMap, tList -> createMonthlyTransactionReport(tList, tagCategories));
    return new TreeMap<>(monthlyReportMap);
  }

  private static Function<ProcessedTransaction, LocalDate> getFirstOfMonth() {
    return t -> {
      var transactionTime = t.getDateTime();
      return LocalDate.of(transactionTime.getYear(), transactionTime.getMonth(), 1);
    };
  }

  private List<IgnoredTransactionsReport> createIgnoredTransactionsReports(TransactionProcessorResult result) {
    var ignoredTransactionToReasonMap = result.getIgnoredTransactions();
    var reasonToTransactionMap = invertMap(ignoredTransactionToReasonMap);
    return reasonToTransactionMap.entrySet().stream()
        .map(ReportCreator::createIgnoredTransactionReport)
        .sorted(comparing(r -> r.getReasonIgnored().toReadableString()))
        .collect(toList());
  }

  private MonthlyTransactionReport createMonthlyTransactionReport(List<ProcessedTransaction> processedTransactions, Map<String, String> tagCategories) {
    processedTransactions.sort(Comparator.comparing(ProcessedTransaction::getDateTime).thenComparing(ProcessedTransaction::getTag));

    var tagMap = processedTransactions.stream().collect(Collectors.groupingBy(ProcessedTransaction::getTag));
    var tagReports = tagMap.entrySet().stream()
        .map(e -> createTagLevelReport(e, tagCategories))
        .sorted(comparing(TagLevelReport::getTag))
        .collect(toList());

    var amountIn = sumTransactionsWithFilter(processedTransactions, ProcessedTransaction::isPositive);
    var amountOut = sumTransactionsWithFilter(processedTransactions, ProcessedTransaction::isNegative);

    var dateToNegativeTransactionMap = processedTransactions.stream().filter(ProcessedTransaction::isNegative).collect(Collectors.groupingBy(t -> t.getDateTime().toLocalDate()));
    var dateToExpenditureMap = Maps.transformValues(dateToNegativeTransactionMap, ReportCreator::sumTransactions);
    var sortedDateToExpenditureMap = new TreeMap<>(dateToExpenditureMap);

    return new MonthlyTransactionReport(processedTransactions, amountIn, amountOut, tagReports, sortedDateToExpenditureMap);
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

  private static TagLevelReport createTagLevelReport(Map.Entry<String, List<ProcessedTransaction>> tagMapEntry, Map<String, String> tagCategories) {
    var tag = tagMapEntry.getKey();
    var tagCategory = tagCategories.get(tag);
    var transactions = tagMapEntry.getValue();

    var sortedTransactions = transactions.stream().sorted(comparing(ProcessedTransaction::getDateTime)).collect(toList());

    var amountIn = sumTransactionsWithFilter(transactions, ProcessedTransaction::isPositive);
    var amountOut = sumTransactionsWithFilter(transactions, ProcessedTransaction::isNegative);

    return new TagLevelReport(tag, tagCategory, amountIn, amountOut, sortedTransactions);
  }

  private static Money sumTransactions(Collection<ProcessedTransaction> transactions) {
    return sumTransactionsWithFilter(transactions, always -> true);
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
