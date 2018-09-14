package uk.co.lucyleach.monzo_transaction_reader.report.excel_writer;

import org.apache.poi.ss.usermodel.Sheet;
import uk.co.lucyleach.monzo_transaction_reader.report.TagLevelReport;
import uk.co.lucyleach.monzo_transaction_reader.report.TransactionReport;

import java.util.List;
import java.util.function.Consumer;

/**
 * User: Lucy
 * Date: 14/09/2018
 * Time: 21:29
 */
public class ExcelSheetWriter_TagReport implements ExcelSheetWriter<TagLevelReport> {
  @Override
  public String getSheetName() {
    return "Tag Report";
  }

  @Override
  public String[] getTitles() {
    return new String[]{"Tag Name", "Total In", "Total Out", "Num Transactions"};
  }

  @Override
  public List<TagLevelReport> getObjects(TransactionReport report) {
    return report.getTagReports();
  }

  @Override
  public Consumer<TagLevelReport> objectWriter(Sheet sheet) {
    return report -> {
      var row = sheet.createRow(sheet.getPhysicalNumberOfRows());
      row.createCell(0).setCellValue(report.getTag());
      row.createCell(1).setCellValue(report.getTotalAmountIn().getAmountInPounds().doubleValue());
      row.createCell(2).setCellValue(report.getTotalAmountOut().getAmountInPounds().doubleValue());
      row.createCell(3).setCellValue(report.getNumTransactions());
    };
  }
}
