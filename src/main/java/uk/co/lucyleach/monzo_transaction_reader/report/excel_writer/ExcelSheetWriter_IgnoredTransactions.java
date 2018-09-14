package uk.co.lucyleach.monzo_transaction_reader.report.excel_writer;

import org.apache.poi.ss.usermodel.Sheet;
import uk.co.lucyleach.monzo_transaction_reader.report.IgnoredTransactionsReport;
import uk.co.lucyleach.monzo_transaction_reader.report.TransactionReport;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.IntStream;

/**
 * User: Lucy
 * Date: 14/09/2018
 * Time: 21:37
 */
public class ExcelSheetWriter_IgnoredTransactions implements ExcelSheetWriter<IgnoredTransactionsReport> {
  @Override
  public String getSheetName() {
    return "Ignored Transactions";
  }

  @Override
  public String[] getTitles() {
    return new String[]{"Reason", "Transaction"};
  }

  @Override
  public List<IgnoredTransactionsReport> getObjects(TransactionReport report) {
    return report.getIgnoredTransactionsReports();
  }

  @Override
  public Consumer<IgnoredTransactionsReport> objectWriter(Sheet sheet) {
    return report -> {
      var firstTagRow = sheet.getPhysicalNumberOfRows(); //Last row index + 1
      var tagRow = sheet.createRow(firstTagRow);
      tagRow.createCell(0).setCellValue(report.getReasonIgnored().toString());
      var allTransactions = report.getIgnoredTransactions();
      IntStream.range(0, allTransactions.size()).forEachOrdered(i -> {
        var row = sheet.createRow(firstTagRow + 1 + i);
        var transaction = allTransactions.get(i);
        row.createCell(0);
        row.createCell(1).setCellValue(transaction.toString());
      });
    };
  }
}
