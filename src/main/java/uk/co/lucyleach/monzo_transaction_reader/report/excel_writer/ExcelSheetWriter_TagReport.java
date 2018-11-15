package uk.co.lucyleach.monzo_transaction_reader.report.excel_writer;

import org.apache.poi.ss.usermodel.Sheet;
import uk.co.lucyleach.monzo_transaction_reader.report.MonthlyTransactionReport;
import uk.co.lucyleach.monzo_transaction_reader.report.TagLevelReport;
import uk.co.lucyleach.monzo_transaction_reader.report.TransactionReport;

import java.util.ArrayList;
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
  List<TagLevelReport> getObjectsFromSplitReport(MonthlyTransactionReport splitReport) {
    var tagReports = new ArrayList<>(splitReport.getTagReports());
    tagReports.sort(ExcelSheetWriter_TagReport::orderReports);
    return tagReports;
  }

  private static int orderReports(TagLevelReport o1, TagLevelReport o2) {
    if(o1.getTag().equals(o2.getTag())) {
      return 0;
    } else if("Income".equals(o1.getTag())) {
      return -1;
    } else if("Income".equals(o2.getTag())) {
      return 1;
    } else {
      return o1.getTotalAmount().compareTo(o2.getTotalAmount());
    }
  }

  @Override
  public String[] getTitles(TransactionReport report) {
    return new String[]{"Classification", "Tag Name", "Total In", "Total Out", "Num Transactions"};
  }

  @Override
  public Consumer<TagLevelReport> objectWriter(Sheet sheet) {
    return report -> {
      var row = sheet.createRow(sheet.getPhysicalNumberOfRows());
      row.createCell(0).setCellValue(report.getTagClassification());
      row.createCell(1).setCellValue(report.getTag());
      row.createCell(2).setCellValue(report.getTotalAmountIn().getAmountInPounds().doubleValue());
      row.createCell(3).setCellValue(report.getTotalAmountOut().getAmountInPounds().doubleValue());
      row.createCell(4).setCellValue(report.getNumTransactions());
    };
  }
}
