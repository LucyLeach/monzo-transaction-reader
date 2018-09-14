package uk.co.lucyleach.monzo_transaction_reader.report.excel_writer;

import org.apache.poi.ss.usermodel.Sheet;
import uk.co.lucyleach.monzo_transaction_reader.report.TagLevelReport;
import uk.co.lucyleach.monzo_transaction_reader.report.TransactionReport;

import java.util.Date;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.IntStream;

/**
 * User: Lucy
 * Date: 14/09/2018
 * Time: 21:30
 */
public class ExcelSheetWriter_DetailedTagReport implements ExcelSheetWriter<TagLevelReport> {

  @Override
  public String getSheetName() {
    return "Detailed Tag Report";
  }

  @Override
  public String[] getTitles() {
    return new String[]{"Tag Name", "Monzo ID", "Date Time", "Amount", "Where"};
  }

  @Override
  public List<TagLevelReport> getObjects(TransactionReport report) {
    return report.getTagReports();
  }

  @Override
  public Consumer<TagLevelReport> objectWriter(Sheet sheet) {
    return report -> {
      var firstTagRow = sheet.getPhysicalNumberOfRows(); //Last row index + 1
      var tagRow = sheet.createRow(firstTagRow);
      tagRow.createCell(0).setCellValue(report.getTag());
      var allTransactions = report.getContributingTransactions();
      IntStream.range(0, allTransactions.size()).forEachOrdered(i -> {
        var row = sheet.createRow(firstTagRow + 1 + i);
        var transaction = allTransactions.get(i);
        row.createCell(0);
        row.createCell(1).setCellValue(transaction.getMonzoId());
        row.createCell(2).setCellValue(Date.from(transaction.getDateTime().toInstant()));
        row.createCell(3).setCellValue(transaction.getAmount().getAmountInPounds().doubleValue());
        row.createCell(4).setCellValue(transaction.getWhere());
      });
    };
  }
}
