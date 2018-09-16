package uk.co.lucyleach.monzo_transaction_reader.report.excel_writer;

import org.apache.poi.ss.usermodel.Sheet;
import uk.co.lucyleach.monzo_transaction_reader.report.TagLevelReport;
import uk.co.lucyleach.monzo_transaction_reader.report.TransactionReport2;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static java.util.stream.Collectors.toMap;

/**
 * User: Lucy
 * Date: 14/09/2018
 * Time: 21:29
 */
public class ExcelSheetWriter_TagReport implements ExcelSheetWriter<TagLevelReport> {
  @Override
  public Map<String, List<TagLevelReport>> getObjectsToWritePerSheet(TransactionReport2 report) {
    return report.getSplitReportsByLabel().entrySet().stream()
        .collect(toMap(e -> "Tag_Report_" + e.getKey(), e -> e.getValue().getTagReports()));
  }

  @Override
  public String[] getTitles() {
    return new String[]{"Tag Name", "Total In", "Total Out", "Num Transactions"};
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
