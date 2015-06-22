package com.objectfrontier.invoice.excel.system;

import com.objectfrontier.invoice.excel.exception.ReportException;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.regex.Pattern;

/**
 * Created by ahariharan on 6/18/15.
 */
public final class Utils {

  static final String INVOICE_FILE_NAME_SUFFIX = "Invoice-Detail-";//2015.xlsx";

  private final Progress progress = Progress.instance();

  private Handler handler;

  public static String getHome() throws ReportException{
    String home = System.getProperty(InvoiceUtil.DROPBOX_HOME, "");
    if (home.equals("")) throw new ReportException ("Please provide your drop box home in VM parameter");
    return home;
  }

  private static Utils utils = new Utils();

  public static Utils getInstance() {return utils;}

  private Utils(){}

  public List<File> getInvoiceFiles() throws ReportException {
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

  public void setHandler(Handler handler) {
    this.handler = handler;
  }

  public Handler getHandler() {
    return this.handler;
  }

  public boolean isInvoiceFileName(String fileName) {
    final Pattern pattern = Pattern.compile(".*-" + INVOICE_FILE_NAME_SUFFIX + ".*xlsx$", Pattern.CASE_INSENSITIVE);
    return pattern.matcher(fileName).matches();
  }

  public String getStackTrace(Exception exception) {
    Writer writer = new StringWriter();
    PrintWriter printWriter = new PrintWriter(writer);
    exception.printStackTrace(printWriter);
    return writer.toString();
  }
}
