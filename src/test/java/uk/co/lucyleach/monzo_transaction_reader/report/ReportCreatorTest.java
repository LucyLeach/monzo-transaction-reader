package uk.co.lucyleach.monzo_transaction_reader.report;

import org.junit.Test;
import uk.co.lucyleach.monzo_transaction_reader.monzo_model.Transaction;
import uk.co.lucyleach.monzo_transaction_reader.output_model.Money;
import uk.co.lucyleach.monzo_transaction_reader.processor.ReasonIgnored;
import uk.co.lucyleach.monzo_transaction_reader.processor.TransactionProcessorResult;

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
    var ignoreTranOut = createMockIgnoredTransaction(-150, "2018-09-23T12:00:00.0Z");
    var declineTranOut = createMockIgnoredTransaction(-300, "2018-09-23T13:00:00.0Z");
    var declineTranOutEarlier = createMockIgnoredTransaction(-520, "2018-09-22T13:00:00.0Z");
    var ignoreTranIn = createMockIgnoredTransaction(137, "2018-09-23T14:00:00.0Z");

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

  private static Transaction createMockIgnoredTransaction(int amount, String timeString) {
    var ignoreTranOut = createMock(Transaction.class);
    expect(ignoreTranOut.getCurrency()).andReturn("GBP").anyTimes();
    expect(ignoreTranOut.getAmount()).andReturn(amount).anyTimes();
    expect(ignoreTranOut.getCreated()).andReturn(timeString).anyTimes();
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