package com.objectfrontier.excel.reports.sales;

import com.objectfrontier.invoice.excel.ExcelInvoiceReader;
import com.objectfrontier.invoice.excel.reports.sales.ExcelSalesReportWriter;
import com.objectfrontier.invoice.excel.system.InvoiceUtil;
import com.objectfrontier.invoice.excel.system.InvoiceUtil.MONTH;
import com.objectfrontier.invoice.excel.system.Utils;
import com.objectfrontier.job.Task;
import com.objectfrontier.localcache.DataCache;
import com.objectfrontier.model.ClientAccount;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static com.objectfrontier.invoice.excel.system.InvoiceUtil.DROPBOX_HOME;
import static org.junit.Assert.*;

/**
 * Created by ahariharan on 6/18/15.
 */
public class UtilsTest {
  Logger log = Logger.getLogger(this.getClass().getName());
  ExcelInvoiceReader invoiceReader ;
  File file;
  Utils utils;
  Handler handler;
  StringBuilder logCollector;
  Task task;
  @Before
  public void setUp() throws Exception {
    logCollector = new StringBuilder();
    handler = new Handler() {
      @Override public void publish(LogRecord record) {
        StringBuilder buffer = new StringBuilder();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss.SSS");
        String time = dateFormat.format(new Date(record.getMillis()));
        buffer.append(time).append("\t");
        buffer.append(record.getLevel().getName()).append("\t");
        buffer.append("[").append(String.format("%s.%s", record.getLoggerName(), record.getSourceMethodName())).append(
                        "]\t");
        buffer.append(record.getMessage());

        logCollector.append(buffer.toString()).append("\n");
      }

      @Override public void flush() {

      }

      @Override public void close() throws SecurityException {

      }
    };

    task = new Task() {
      @Override public void add() {

      }

      @Override public void done() {

      }

      @Override public void restart() {

      }
    };

    file = new File("/Users/ahariharan/Documents/ofs/SalesReport-TestData/INV/All-Client-JUnit-Test-Sales-Report.xlsx");
    utils = Utils.getInstance();
    utils.setHandler(handler);
    System.setProperty(DROPBOX_HOME, "/Users/ahariharan/Documents/ofs/SalesReport-TestData/INV");
    invoiceReader = new ExcelInvoiceReader(task);

  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testbuildAltisourceSalesReport() throws Exception {
    String path = "/Users/ahariharan/Documents/ofs/SalesReport-TestData/INV/INV.Altisource";
    System.setProperty(DROPBOX_HOME, path);
    file = new File("/Users/ahariharan/Documents/ofs/SalesReport-TestData/INV/Altisource-SalesReport.xlsx");
    buildSalesReport();
    saveLog("altisource");
  }

  @Test
  public void testHealthportSalesReport() throws Exception {
    String path = "/Users/ahariharan/Documents/ofs/SalesReport-TestData/INV/INV.Healthport";
    System.setProperty(DROPBOX_HOME, path);
    file = new File("/Users/ahariharan/Documents/ofs/SalesReport-TestData/INV/Healthport-SalesReport.xlsx");
    buildSalesReport();
    saveLog("healthport");
  }

  @Test
  public void testLancopeSalesReport() throws Exception {
    String path = "/Users/ahariharan/Documents/ofs/SalesReport-TestData/INV/INV.Lancope";
    System.setProperty(DROPBOX_HOME, path);
    file = new File("/Users/ahariharan/Documents/ofs/SalesReport-TestData/INV/Lancope-SalesReport.xlsx");
    buildSalesReport();
    saveLog("lancope");
  }

  private void saveLog(String name) throws Exception {
    String fileName = "/Users/ahariharan/Documents/ofs/SalesReport-TestData/INV/" + name + ".log";
    FileOutputStream fos = new FileOutputStream(new File(fileName));
    fos.write(logCollector.toString().getBytes());
    fos.flush();
    fos.close();

  }

  @Test
  public void testSalesReport() throws Exception {
    buildSalesReport();
    saveLog("All-Accounts-JUnit-Test");
  }

  public void buildSalesReport() throws Exception {
    generateReport(MONTH.Jan, 2015);
    generateReport(MONTH.Feb, 2015);
    generateReport(MONTH.Mar, 2015);
    generateReport(MONTH.Apr, 2015);
    generateReport(MONTH.Jun, 2015);

  }

  private void generateReport(MONTH month, int year) throws Exception {
    Map<String, ClientAccount> clientAccounts = invoiceReader.parseAllClientInvoice(year, month);
    ExcelSalesReportWriter reportWriter = new ExcelSalesReportWriter(clientAccounts, task);
    XSSFWorkbook workbook = reportWriter.getSalesReport(loadWorkbook(), year, month);
    FileOutputStream fos = new FileOutputStream(file);
    workbook.write(fos);
    fos.flush();
    fos.close();

  }

  @Test
  public void testReadClientInvoices() throws Exception {
//    String path = "/Users/ahariharan/Documents/ofs/SalesReport-TestData/INV/INV.Birch";
    String path = "/Users/ahariharan/Documents/ofs/SalesReport-TestData/INV/INV.Altisource";
//    String path = "/Users/ahariharan/Documents/ofs/SalesReport-TestData/INV/INV.Healthport";
//    String path = "/Users/ahariharan/Documents/ofs/SalesReport-TestData/INV/INV.Lancope";
//    String path = "/Users/ahariharan/Documents/ofs/SalesReport-TestData/INV";
    System.setProperty(DROPBOX_HOME, path);

    Map<String, ClientAccount> clientAccountDetails = readInvoiceFile(MONTH.Jan, 2015);
    System.out.println(clientAccountDetails);
    //    readInvoiceFile(MONTH.Feb, 2015);
//    readInvoiceFile(MONTH.Mar, 2015);
//    readInvoiceFile(MONTH.Apr, 2015);
//    readInvoiceFile(MONTH.Jun, 2015);
  }

  private Map<String, ClientAccount> readInvoiceFile(MONTH month, int year) throws Exception {
    return invoiceReader.parseAllClientInvoice(year, month);
  }

  private XSSFWorkbook loadWorkbook() {
    try {
      return new XSSFWorkbook(new FileInputStream(file));
    } catch (Exception ex) {
      return null;
    }
  }

  @Test
  public void testWorksheet() throws Exception {
    XSSFWorkbook book = new XSSFWorkbook();
    XSSFSheet sheet = book.createSheet("test");
    int index = book.getSheetIndex("test");
    assertNotNull(sheet);

    sheet = book.getSheet("test1");
    assertNull(sheet);

    sheet = book.getSheet("test");
    assertNotNull(sheet);

    book.removeSheetAt(index);
    sheet = book.getSheet("test");
    assertNull(sheet);


  }
}
