package uk.co.lucyleach.monzo_transaction_reader.report.excel_writer;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;
import uk.co.lucyleach.monzo_transaction_reader.output_model.Money;
import uk.co.lucyleach.monzo_transaction_reader.report.TransactionReport;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

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
  public String getSheetName() {
    return "Expenditure by Date";
  }

  @Override
  public String[] getTitles() {
    return new String[]{"Date", "Expenditure"};
  }

  @Override
  public List<Map.Entry<LocalDate, Money>> getObjects(TransactionReport report) {
    return List.copyOf(report.getExpenditureByDate().entrySet());
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
