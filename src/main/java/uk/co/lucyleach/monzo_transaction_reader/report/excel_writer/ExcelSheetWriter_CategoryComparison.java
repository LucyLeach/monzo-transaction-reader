package uk.co.lucyleach.monzo_transaction_reader.report.excel_writer;

import com.google.common.collect.ImmutableList;
import org.apache.poi.ss.usermodel.Sheet;
import uk.co.lucyleach.monzo_transaction_reader.report.CategoryReport;
import uk.co.lucyleach.monzo_transaction_reader.report.TransactionReport;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * User: Lucy
 * Date: 01/10/2018
 * Time: 20:25
 */
public class ExcelSheetWriter_CategoryComparison implements ExcelSheetWriter<CategoryReport> {
  @Override
  public Map<String, List<CategoryReport>> getObjectsToWritePerSheet(TransactionReport report) {
    return Map.of("Category Comparison", report.getCategoryReports());
  }

  @Override
  public String[] getTitles(TransactionReport report) {
    return ImmutableList.<String>builder().add("Categories").addAll(report.getMonthlyReportsByLabel().keySet()).build().toArray(new String[]{});
  }

  @Override
  public Consumer<CategoryReport> objectWriter(Sheet sheet) {
    return categoryReport -> {
      var row = sheet.createRow(sheet.getPhysicalNumberOfRows());
      row.createCell(0).setCellValue(categoryReport.getCategory());
      categoryReport.getAmountOutBySplit().stream().forEachOrdered(amount -> {
        row.createCell(row.getPhysicalNumberOfCells()).setCellValue(amount);
      });
    };
  }
}
