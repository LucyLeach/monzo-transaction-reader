package uk.co.lucyleach.monzo_transaction_reader.report.excel_writer;

import org.apache.poi.ss.usermodel.Sheet;
import uk.co.lucyleach.monzo_transaction_reader.report.IgnoredTransactionsReport;
import uk.co.lucyleach.monzo_transaction_reader.report.TransactionReport;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.IntStream;

/**
 * User: Lucy
 * Date: 14/09/2018
 * Time: 21:37
 */
public class ExcelSheetWriter_IgnoredTransactions implements ExcelSheetWriter<IgnoredTransactionsReport> {
  @Override
  public Map<String, List<IgnoredTransactionsReport>> getObjectsToWritePerSheet(TransactionReport report) {
    return Map.of("Ignored Transactions", report.getIgnoredTransactionsReports());
  }

  @Override
  public String[] getTitles(TransactionReport report) {
    return new String[]{"Reason", "Transaction ID", "Amount", "Currency", "Created", "Notes", "Description", "Merchant", "Counterparty"};
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
        row.createCell(1).setCellValue(transaction.getId());
        row.createCell(2).setCellValue(transaction.getAmount());
        row.createCell(3).setCellValue(transaction.getCurrency());
        row.createCell(4).setCellValue(transaction.getCreated());
        row.createCell(5).setCellValue(transaction.getNotes());
        row.createCell(6).setCellValue(transaction.getDescription());
        if(transaction.getMerchant() != null && transaction.getMerchant().getName() != null) {
          row.createCell(7).setCellValue(transaction.getMerchant().getName());
        }
        if(transaction.getCounterparty() != null && transaction.getCounterparty().isNonEmpty()) {
          row.createCell(8).setCellValue(transaction.getCounterparty().getAccountId());
        }
      });
    };
  }
}
