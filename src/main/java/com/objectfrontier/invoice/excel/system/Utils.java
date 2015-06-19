package com.objectfrontier.invoice.excel.system;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by ahariharan on 6/18/15.
 */
public final class Utils {

  static final String DROPBOX_HOME = "DROPBOX_HOME";

  static final String INVOICE_FILE_NAME_SUFFIX = "Invoice-Detail-";//2015.xlsx";

  public static String getHome() {return System.getProperty(DROPBOX_HOME, "/Users/ahariharan/Documents/ofs/confidential/INV/util.test/INV");}

  private static Utils utils = new Utils();

  public static Utils getInstance() {return utils;}

  private Utils(){}

  public List<File> getInvoiceFiles() {
    List<File> invoiceFileList = new ArrayList();
    File file = new File(getHome());
    getFile(file, INVOICE_FILE_NAME_SUFFIX, invoiceFileList);
    return invoiceFileList;
  }

  private void getFile(File file, String invoiceFileNameSuffix, List<File> invoiceFileList) {
    if (!file.isDirectory()) return;
    File[] files = file.listFiles();

    for (File eachFile:files) {
      if (eachFile.isDirectory()) {
        getFile(eachFile, invoiceFileNameSuffix, invoiceFileList);
      }

      String fileName = eachFile.getName();
      if (fileName.startsWith("~") || !isInvoiceFileName(fileName))
        continue;

      System.out.println(eachFile.getAbsolutePath());
      invoiceFileList.add(eachFile);
    }
  }

  public boolean isInvoiceFileName(String fileName) {
    final Pattern pattern = Pattern.compile(".*-" + INVOICE_FILE_NAME_SUFFIX + ".*xlsx$", Pattern.CASE_INSENSITIVE);
    return pattern.matcher(fileName).matches();
  }
}
