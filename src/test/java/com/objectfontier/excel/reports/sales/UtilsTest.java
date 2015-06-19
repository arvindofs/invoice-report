package com.objectfontier.excel.reports.sales;

import com.objectfrontier.invoice.excel.reports.sales.ExcelInvoiceReader;
import com.objectfrontier.invoice.excel.system.InvoiceUtil.MONTH;
import com.objectfrontier.invoice.excel.system.Utils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.List;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by ahariharan on 6/18/15.
 */
public class UtilsTest {
  Logger log = Logger.getLogger(this.getClass().getName());
  ExcelInvoiceReader invoiceReader ;
  Utils utils;
  @Before
  public void setUp() throws Exception {
    invoiceReader = new ExcelInvoiceReader();
    utils = Utils.getInstance();
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void getFileLists() throws Exception {
    List<File> fileList = utils.getInvoiceFiles();
    assertEquals(3, fileList.size());
    for(File file : fileList) {
      log.info(file.getCanonicalPath());
    }
  }

  @Test
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
    invoiceReader.buildSalesReport(2015, MONTH.May);
  }

  @Test
  public void testDoubleValue() throws Exception {
    double d = 3.4;
    assertEquals(3.4d, d, 0);

  }
}
