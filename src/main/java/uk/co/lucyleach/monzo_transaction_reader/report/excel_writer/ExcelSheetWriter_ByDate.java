package uk.co.lucyleach.monzo_transaction_reader.report.excel_writer;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;
import uk.co.lucyleach.monzo_transaction_reader.output_model.Money;
import uk.co.lucyleach.monzo_transaction_reader.report.TransactionReport2;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * User: Lucy
 * Date: 14/09/2018
 * Time: 21:41
 */
public class ExcelSheetWriter_ByDate implements ExcelSheetWriter<Map.Entry<LocalDate, Money>> {
  private final CellStyle dateStyle;

  public ExcelSheetWriter_ByDate(CellStyle dateStyle) {
    this.dateStyle = dateStyle;
  }

  @Override
  public Map<String, List<Map.Entry<LocalDate, Money>>> getObjectsToWritePerSheet(TransactionReport2 report) {
    return report.getSplitReportsByLabel().entrySet().stream()
        .collect(Collectors.toMap(e -> "Expenditure_By_Date_" + e.getKey(), e -> List.copyOf(e.getValue().getExpenditureByDate().entrySet())));
  }

  @Override
  public String[] getTitles() {
    return new String[]{"Date", "Expenditure"};
  }


  @Override
  public Consumer<Map.Entry<LocalDate, Money>> objectWriter(Sheet sheet) {
    return entry -> {
      var row = sheet.createRow(sheet.getPhysicalNumberOfRows());

      var dateCell = row.createCell(0);
      dateCell.setCellValue(Date.from(entry.getKey().atStartOfDay(ZoneId.systemDefault()).toInstant()));
      dateCell.setCellStyle(dateStyle);

      row.createCell(1).setCellValue(entry.getValue().getAmountInPounds().doubleValue());
    };
  }
}
