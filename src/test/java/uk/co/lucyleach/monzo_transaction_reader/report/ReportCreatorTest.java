package uk.co.lucyleach.monzo_transaction_reader.report;

import org.junit.Test;
import uk.co.lucyleach.monzo_transaction_reader.monzo_model.Transaction;
import uk.co.lucyleach.monzo_transaction_reader.output_model.Money;
import uk.co.lucyleach.monzo_transaction_reader.output_model.SaleTransaction;
import uk.co.lucyleach.monzo_transaction_reader.processor.ProcessorResult;
import uk.co.lucyleach.monzo_transaction_reader.processor.ReasonIgnored;
import uk.co.lucyleach.monzo_transaction_reader.processor.TransactionProcessorResult;
import uk.co.lucyleach.monzo_transaction_reader.utils.Pair;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;
import static uk.co.lucyleach.monzo_transaction_reader.processor.ProcessorResult.createIgnoredResult;

/**
 * User: Lucy
 * Date: 22/11/2018
 * Time: 10:31
 */
public class ReportCreatorTest {
  private static final ReportCreator UNDER_TEST = new ReportCreator();

  @Test
  public void testIgnoredTransactionsOnly() {
    var ignoreTranOut = createOriginalTransaction(-150, timeWithHours(12));
    var declineTranOut = createOriginalTransaction(-300, timeWithHours(13));
    var declineTranOutEarlier = createOriginalTransaction(-520, timeWithHours(2));
    var ignoreTranIn = createOriginalTransaction(137, timeWithHours(14));

    var toTest = new TransactionProcessorResult(Set.of(
        createIgnoredResult(ignoreTranOut, ReasonIgnored.IGNORE_TAG),
        createIgnoredResult(declineTranOut, ReasonIgnored.DECLINED),
        createIgnoredResult(declineTranOutEarlier, ReasonIgnored.DECLINED),
        createIgnoredResult(ignoreTranIn, ReasonIgnored.IGNORE_TAG)
    ));

    var report = UNDER_TEST.create(toTest, Map.of());
    checkForNulls(report);

    assertTrue(report.getMonthlyReportsByLabel().isEmpty());
    assertTrue(report.getAllTagsSortedWithCategories().isEmpty());
    assertTrue(report.getCategoryReports().isEmpty());

    assertEquals(2, report.getIgnoredTransactionsReports().size());

    var ignoreTagReport = report.getIgnoredTransactionsReports().get(0);
    assertEquals(ReasonIgnored.IGNORE_TAG, ignoreTagReport.getReasonIgnored());
    assertEquals(new Money(-13, "GBP"), ignoreTagReport.getTotalAmount());
    assertEquals(List.of(ignoreTranOut, ignoreTranIn), ignoreTagReport.getIgnoredTransactions());

    var declineTagReport = report.getIgnoredTransactionsReports().get(1);
    assertEquals(ReasonIgnored.DECLINED, declineTagReport.getReasonIgnored());
    assertEquals(new Money(-820, "GBP"), declineTagReport.getTotalAmount());
    assertEquals(List.of(declineTranOutEarlier, declineTranOut), declineTagReport.getIgnoredTransactions());
  }

  @Test
  public void testCategories() {
    var unknownTag = "Tag1";
    var tag2 = "Tag2";
    var tag3 = "Tag3";
    var tag4 = "Tag4";
    var category1 = "Cat1";
    var category2 = "Cat2";

    var toTest = new TransactionProcessorResult(Set.of(
        createSingleTranProcessorResult(-348, unknownTag, timeWithHours(1)),
        createSingleTranProcessorResult(-213, tag2, timeWithHours(2)),
        createSingleTranProcessorResult(-467, tag3, timeWithHours(2)),
        createSingleTranProcessorResult(-167, tag4, timeWithHours(4))
    ));
    var report = UNDER_TEST.create(toTest, Map.of(tag2, category1, tag3, category2, tag4, category2));

    checkForNulls(report);
    assertTrue(report.getIgnoredTransactionsReports().isEmpty());

    assertEquals(3, report.getCategoryReports().size());

    var category1Report = report.getCategoryReports().get(0);
    assertEquals(category1, category1Report.getCategory());
    assertEquals(List.of(-2.13), category1Report.getAmountOutByMonth());

    var category2Report = report.getCategoryReports().get(1);
    assertEquals(category2, category2Report.getCategory());
    assertEquals(List.of(-6.34), category2Report.getAmountOutByMonth());

    var unknownCategoryReport = report.getCategoryReports().get(2);
    assertEquals(unknownTag, unknownCategoryReport.getCategory());
    assertEquals(List.of(-3.48), unknownCategoryReport.getAmountOutByMonth());

    var tagsWithCategories = report.getAllTagsSortedWithCategories();
    var expectedTagsWithCategories = List.of(
        new Pair<>(unknownTag, unknownTag),
        new Pair<>(tag2, category1),
        new Pair<>(tag3, category2),
        new Pair<>(tag4, category2)
    );
    assertEquals(expectedTagsWithCategories, tagsWithCategories);
  }

  private static ZonedDateTime timeWithHours(int hours) {
    return ZonedDateTime.of(2018, 11, 22, hours, 0, 0, 0, ZoneId.of("UTC"));
  }

  private static ProcessorResult createSingleTranProcessorResult(int amount, String tag, ZonedDateTime created) {
    var origTran = createMock(Transaction.class);
    replay(origTran);
    var processedTran = new SaleTransaction("ID-" + created, created, new Money(amount, "GBP"), "Merchant", tag);
    return ProcessorResult.createProcessedResult(origTran, Set.of(processedTran));
  }

  private static Transaction createOriginalTransaction(int amount, ZonedDateTime time) {
    var ignoreTranOut = createMock(Transaction.class);
    expect(ignoreTranOut.getCurrency()).andReturn("GBP").anyTimes();
    expect(ignoreTranOut.getAmount()).andReturn(amount).anyTimes();
    expect(ignoreTranOut.getCreated()).andReturn(time.toString()).anyTimes();
    replay(ignoreTranOut);
    return ignoreTranOut;
  }

  private static void checkForNulls(TransactionReport report) {
    assertNotNull(report);
    assertNotNull(report.getAllTagsSortedWithCategories());
    assertNotNull(report.getCategoryReports());
    assertNotNull(report.getMonthlyReportsByLabel());
    assertNotNull(report.getIgnoredTransactionsReports());
  }
}