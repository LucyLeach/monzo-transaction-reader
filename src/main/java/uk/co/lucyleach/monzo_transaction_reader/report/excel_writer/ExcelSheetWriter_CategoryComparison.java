package uk.co.lucyleach.monzo_transaction_reader.report.excel_writer;

import com.google.common.collect.ImmutableList;
import org.apache.poi.ss.usermodel.Sheet;
import uk.co.lucyleach.monzo_transaction_reader.report.CategoryReport;
import uk.co.lucyleach.monzo_transaction_reader.report.TransactionReport;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static java.util.stream.Collectors.toList;

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
    var titles = report.getMonthlyReportsByFirstOfMonth().keySet().stream().sorted().map(d -> d.getMonth().toString().substring(0, 3).toUpperCase() + d.getYear()).collect(toList());
    return ImmutableList.<String>builder().add("Categories").addAll(titles).build().toArray(new String[]{});
  }

  @Override
  public Consumer<CategoryReport> objectWriter(Sheet sheet) {
    return categoryReport -> {
      var row = sheet.createRow(sheet.getPhysicalNumberOfRows());
      row.createCell(0).setCellValue(categoryReport.getCategory());
      categoryReport.getAmountOutByMonth().stream().forEachOrdered(amount -> row.createCell(row.getPhysicalNumberOfCells()).setCellValue(amount));
    };
  }
}
