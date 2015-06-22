package com.objectfrontier.excel.reports.sales;

import com.objectfrontier.invoice.excel.reports.sales.ExcelInvoiceReader;
import com.objectfrontier.invoice.excel.reports.sales.ExcelSalesReportWriter;
import com.objectfrontier.invoice.excel.system.InvoiceUtil.MONTH;
import com.objectfrontier.invoice.excel.system.Utils;
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
import java.util.List;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

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
  @Before
  public void setUp() throws Exception {

    handler = new Handler() {
      @Override public void publish(LogRecord record) {

      }

      @Override public void flush() {

      }

      @Override public void close() throws SecurityException {

      }
    };

    file = new File("/Users/ahariharan/Documents/ofs/Sales-Report.xlsx");
    utils = Utils.getInstance();
    utils.setHandler(handler);
    invoiceReader = new ExcelInvoiceReader();

  }

  @After
  public void tearDown() throws Exception {
  }

  @Ignore
  public void getFileLists() throws Exception {
    List<File> fileList = utils.getInvoiceFiles();
    assertEquals(3, fileList.size());
    for(File file : fileList) {
      log.info(file.getCanonicalPath());
    }
  }

  @Ignore
  public void testFileNameMatcher() throws Exception {
    boolean isMatching = utils.isInvoiceFileName("Lancope-Monthly-Invoice-Detail-2015.xlsx");
    assertTrue(isMatching);

    isMatching = utils.isInvoiceFileName("Birch-Consolidated SOW-Monthly-Invoice-Detail-2015.xlsx");
    assertTrue(isMatching);

    isMatching = utils.isInvoiceFileName("Birch-DotNet SOW-Monthly-Invoice-Detail-2015.xlsx");
    assertTrue(isMatching);

    isMatching = utils.isInvoiceFileName("Ezhumalai-Template.xlsx");
    assertFalse(isMatching);

  }

  @Test
  public void testbuildSalesReport() throws Exception {
    generateReport(MONTH.Jan, 2015);
    generateReport(MONTH.Feb, 2015);
    generateReport(MONTH.Mar, 2015);
    generateReport(MONTH.Apr, 2015);
    generateReport(MONTH.Jun, 2015);

  }

  private void generateReport(MONTH month, int year) throws Exception {
    Map<String, ClientAccount> clientAccounts = invoiceReader.buildSalesReport(year,month);
    ExcelSalesReportWriter reportWriter = new ExcelSalesReportWriter(clientAccounts);
    XSSFWorkbook workbook = reportWriter.getSalesReport(loadWorkbook(), year, month);
    FileOutputStream fos = new FileOutputStream(file);
    workbook.write(fos);;
    fos.flush();
    fos.close();
  }

  private XSSFWorkbook loadWorkbook() {
    try {
      return new XSSFWorkbook(new FileInputStream(file));
    } catch (Exception ex) {
      return null;
    }
  }

  private void saveWorkbook(XSSFWorkbook workbook) throws Exception {

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
