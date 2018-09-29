package uk.co.lucyleach.monzo_transaction_reader.report.excel_writer;

import org.apache.poi.ss.usermodel.Sheet;
import uk.co.lucyleach.monzo_transaction_reader.report.SplitTransactionReport;
import uk.co.lucyleach.monzo_transaction_reader.report.TagLevelReport;

import java.util.List;
import java.util.function.Consumer;

/**
 * User: Lucy
 * Date: 14/09/2018
 * Time: 21:29
 */
public class ExcelSheetWriter_TagReport extends ExcelSheetWriter_FromSplitReports<TagLevelReport> {
  @Override
  String getSheetName() {
    return "Tag_Report";
  }

  @Override
  List<TagLevelReport> getObjectsFromSplitReport(SplitTransactionReport splitReport) {
    return splitReport.getTagReports();
  }

  @Override
  public String[] getTitles() {
    return new String[]{"Tag Name", "Classification", "Total In", "Total Out", "Num Transactions"};
  }

  @Override
  public Consumer<TagLevelReport> objectWriter(Sheet sheet) {
    return report -> {
      var row = sheet.createRow(sheet.getPhysicalNumberOfRows());
      row.createCell(0).setCellValue(report.getTag());
      row.createCell(1).setCellValue(report.getTagClassification());
      row.createCell(2).setCellValue(report.getTotalAmountIn().getAmountInPounds().doubleValue());
      row.createCell(3).setCellValue(report.getTotalAmountOut().getAmountInPounds().doubleValue());
      row.createCell(4).setCellValue(report.getNumTransactions());
    };
  }
}
