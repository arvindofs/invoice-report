package com.objectfrontier.invoice.excel.reports.sales;

import com.objectfrontier.invoice.excel.exception.ReportException;
import com.objectfrontier.invoice.excel.system.InvoiceUtil;
import com.objectfrontier.invoice.excel.system.Progress;
import com.objectfrontier.invoice.excel.system.Utils;
import com.objectfrontier.localcache.DataCache;
import com.objectfrontier.model.*;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static com.objectfrontier.invoice.excel.system.InvoiceUtil.*;

/**
 * Created by ahariharan on 6/18/15.
 */
public class ExcelInvoiceReader {

  private List<File> invoiceFiles;
  private Logger log = Logger.getLogger(this.getClass().getSimpleName());

  private DataCache cache = new DataCache();

  private XSSFWorkbook workbook;
  private XSSFSheet reportingMonthSheet;
  private XSSFSheet settingsSheet;

  private int reportingYear;
  private InvoiceUtil.MONTH reportingMonth;

  private int currentRow;

  private File currentFile;
  private ClientAccount currentClientAccount;
  private Project currentProject;

  private final Progress progress = Progress.instance();

  private Utils utils;

  public ExcelInvoiceReader() {
    utils = Utils.getInstance();
    log.addHandler(utils.getHandler());
  }

  private void init() throws ReportException {
    resetCurrentRow();
    cache.clientAccountCache.clear();
    cache.projectCache.clear();
    currentProject = null;
    currentClientAccount = null;
    invoiceFiles = Utils.getInstance().getInvoiceFiles();
  }

  public Map<String, ClientAccount> buildSalesReport(int year, InvoiceUtil.MONTH month) throws ReportException{
    if (year < 2015) throw new ReportException("Year to build sales report must be 2015 and above");
    this.reportingYear = year;
    this.reportingMonth = month;
    init();

    log("Collectiong data for sales report");
    for(File file : invoiceFiles) {
      resetCurrent();
      processInvoice(file);
    }

    return cache.clientAccountCache;
  }

  private void resetCurrent() {
    resetCurrentRow();
    currentClientAccount = null;
    currentProject = null;
  }

  private String getSettingsSheetName() {
    return "Settings";
  }

  private String getReportingMonthSheetName() {
    return reportingMonth.toString() + reportingYear;
  }

  private void resetCurrentRow() {
    currentRow = 0;
  }

  private FileInputStream loadInvoice(File invoiceFile) {
    try {
      return new FileInputStream(invoiceFile);

    } catch (FileNotFoundException e) {
      log("Sorry I Could not process file " + invoiceFile.getAbsolutePath());
      log(e.getMessage());
      log(e);
    }
    return null;
  }

  private void loadWorkbook(FileInputStream fileInputStream) {
    log("Loading Workbook");
    try {
      workbook = new XSSFWorkbook(fileInputStream);
    } catch (IOException e) {
      log("I'm bugged, but don't worry this is not critical.  I'm moving forwarad" + e.getMessage());
      log(e);
      workbook = null;
    }
  }

  private void loadSheets() {
    log("Loading Sheets");
    settingsSheet = workbook.getSheet(getSettingsSheetName());
    reportingMonthSheet = workbook.getSheet(getReportingMonthSheetName());
  }

  private void processInvoice(File invoiceFile) {
    FileInputStream fileInputStream = loadInvoice(invoiceFile);
    currentFile = invoiceFile;
    if (fileInputStream == null) return;
    log("Started processing invoice sheet");
    loadWorkbook(fileInputStream);
    if (workbook == null) return;
    loadSheets();
    if(reportingMonthSheet == null || settingsSheet ==null) return;
    try {
      readClientAccount();
    }  catch (ReportException exception) {
      log("I'm bugged, I fear I can't read further to analyze " +invoiceFile.getAbsolutePath());
      log("Share the root cause: " + exception.getMessage());
      log(exception);
    }
  }

  private void readClientAccount() throws ReportException {
    log("Hold on checking client details");
    if (!getString(getCurrentRow(), CLIENT_NAME_LABEL_COL_INDEX).equals(CLIENT_NAME_LABEL))
      throw new ReportException("Monthly invoice sheet is not in valid format.");

    String clientName = getString(getCurrentRow(), CLIENT_NAME_COL_INDEX);
    currentClientAccount = cache.getClient(clientName);
    if (currentClientAccount == null) {
      addClient(clientName);
    } else {
      skipRows(0);
    }
    skipRows(4);
    readProjectInvoice();
//    log(currentClientAccount.toString());
  }

  private void addClient(String clientName) {
    log("I'm caching " + clientName + " details so I can work efficiently for you!!!");
    currentClientAccount = cache.addClient(clientName);
    currentClientAccount.code = getString(getNextRow(), CLIENT_CODE_COL_INDEX);
  }

  private void readProjectInvoice() throws ReportException {
    log("Ssshhhh.... about to read project invoice details" + (currentRow < getLastRowIndex()-1));

    while (currentRow < getLastRowIndex()-1) {
      log("Fetch row = " + (currentRow+1) + " of " + getLastRowIndex());
      if (getCurrentRow() == null) break;

      if (SOW_ID_LABEL.equals(getString(getCurrentRow(), SOW_ID_LABEL_COL_INDEX))) {
        skipRows(1);
        String code = getString(getCurrentRow(), SOW_CODE_COL_INDEX);
        currentProject = cache.getProject(code);
        rewind(1);
        if (currentProject == null) {
          addProject(code);
        } else {
          log("Project already exists in cache");
          skipRows(0);
        }
        skipRows(4);
        addEmployees();
      }

      getNextRow();

      try {

        if (SHADOW_RESOURCE_LABEL.equals(getString(getCurrentRow(), SHADOW_RESOURCE_LABEL_COL_INDEX))) {
          addShadowResources();
        }
      } catch (NullPointerException npe) {
        log("Looks like no shadow resources in this project...");
      }

    }
  }

  private void addProject(String code) throws ReportException {
    log("Adding project to cache");
    currentProject = cache.addProject(code);
    currentProject.id = getString(getCurrentRow(), SOW_ID_COL_INDEX);
    currentProject.startDate = getDate(getCurrentRow(), SOW_START_DATE_COL_INDEX);
    currentProject.name = getString(getNextRow(), SOW_NAME_COL_INDEX);
    currentProject.endDate = getDate(getCurrentRow(), SOW_END_DATE_COL_INDEX);
    currentProject.rate = getRate();
    currentClientAccount.projects.add(currentProject);
  }

  private void addEmployees() throws ReportException {
    log("I'm about to read employee details from invoice" + (currentRow < getLastRowIndex() -1));
    while (currentRow < getLastRowIndex() -1) {
      log("Fetch row = " + (currentRow + 1) + " of " + getLastRowIndex());
      log("Fetching employee from row " + (currentRow + 1));
      Employee employee = getEmployee();
      if (employee == null) return;
      log("Adding employee " + employee.firstName + " " + employee.lastName);
//      log("Added employee: " + employee);
      currentProject.employees.add(employee);
      if (SOW_TOTAL_LABEL.equals(getString(getNextRow(), SOW_TOTAL_LABEL_COL_INDEX))) break;
    }
    skipRows(2);
  }

  private Employee getEmployee() throws ReportException {
    try {
//      log ("ROW --> " + getCurrentRow());
      getNumeric(getCurrentRow(), 1);
    } catch (NullPointerException npe) {
      log("No more row exits, returning back");
      return null;
    }

    Employee employee = new Employee();
    employee.role = getString(getCurrentRow(), RESOURCE_ROLE_COL_INDEX);
    employee.location = getString(getCurrentRow(), RESOURCE_LOCATION_COL_INDEX);
    employee.firstName = getString(getCurrentRow(), RESOURCE_FIRST_NAME_COL_INDEX);
    employee.lastName = getString(getCurrentRow(), RESOURCE_LAST_NAME_COL_INDEX);
    employee.shadow = false;
    Billing billing = new Billing();
    billing.billed = getNumeric(getCurrentRow(), RESOURCE_INVOICE_AMOUNT_COL_INDEX);
    billing.billiablePercent = getNumeric(getCurrentRow(), RESOURCE_BILLABLE_PERCENT_COL_INDEX) * 100;
    billing.ptoDays = getNumeric(getCurrentRow(), RESOURCE_PTO_COUNT_COL_INDEX);
    billing.endDate = getDate(getCurrentRow(), RESOURCE_END_DATE_COL_INDEX);
    billing.billableDays = getNumeric(getCurrentRow(), RESOURCE_NUM_DAYS_WORKED_COL_INDEX);
    billing.startDate = getDate(getCurrentRow(), RESOURCE_START_DATE_COL_INDEX);
    billing.rate = getRate().get(employee.location);
    billing.workLocation = employee.location;
    employee.billing = billing;
    log(" name --> " + employee.firstName + " " + employee.lastName);
    progress.addActivity();
    return employee;
  }

  private void addShadowResources() throws ReportException {
    skipRows(1);
    while (currentRow < getLastRowIndex() -1) {
      log("Fetching shadow resource data");
      Employee employee = getEmployee();
      if (employee == null) return;
      log("Shadow resource name = " + employee.firstName + " " + employee.lastName);
      employee.shadow = true;
      String code = getString(getCurrentRow(), SHADOW_RESOURCE_SOW_CODE_COL_INDEX);
      Project project = cache.getProject(code);
      if (project != null) {
        log("Adding shadow resource to project employee list");
        project.employees.add(employee);
      } else {
        log("No valid project assigned to shadow resource , hence not adding " + employee.firstName + " "
                        + employee.lastName);
      }
      if (SOW_TOTAL_LABEL.equals(getString(getNextRow(), SOW_TOTAL_LABEL_COL_INDEX))) break;
      if (getCurrentRow().getCell(1) == null) break;
    }

  }

  private void skipRows(int count) {
    log("skipping " + (count + 1) + " rows");
    currentRow += count + 1;
  }

  private void rewind(int count) {
    currentRow -= (count + 1);
  }

  private XSSFRow getNextRow() {
    XSSFRow row = reportingMonthSheet.getRow(++currentRow);
    log("Fetching Row = " + (currentRow+1));
    return row;
  }

  private XSSFRow getCurrentRow() {
    XSSFRow row = reportingMonthSheet.getRow(currentRow);
    log("Current Row = " + (currentRow +1));//+ " row data is " + row);
    return row;
  }

  private Rate getRate() throws ReportException{
    Rate workbookRate = new Rate();
    for (int i = SETTINGS_LOCATION_ROW_START_INDEX; i <= SETTINGS_LOCATION_ROW_END_INDEX; i++) {
      XSSFRow row = settingsSheet.getRow(i);
      String location = getString(row, SETTINGS_LOCATION_LABEL_COL_INDEX);
      if (location.trim().length() == 0) break;
      double rate = getNumeric(row, SETTINGS_LOCATION_RATE_COL_INDEX);
      workbookRate.put(location, rate);
    }
    return workbookRate;
  }

  private int getLastRowIndex() {
    return reportingMonthSheet.getLastRowNum();
  }

  private String getString(XSSFRow row, int colIndex) {
    XSSFCell cell = row.getCell(colIndex);
    if (cell == null) return "";
    return cell.getStringCellValue() == null ? "" : cell.getStringCellValue();
  }

  private double getNumeric(XSSFRow row, int colIndex)  {
    XSSFCell cell = row.getCell(colIndex);
    return cell.getNumericCellValue();
  }

  private Date getDate(XSSFRow row, int colIndex) {
    XSSFCell cell = row.getCell(colIndex);
    return cell.getDateCellValue();
  }

  private void log(Object content) {
    String prefix = currentFile == null ? "" : currentFile.getName() + ": ";
    prefix = prefix + getReportingMonthSheetName() + ": ";
    if (content instanceof Exception) {
      content = "\nERROR: " + utils.getStackTrace((Exception)content);
    }
    log.info(prefix + content);
  }
}
