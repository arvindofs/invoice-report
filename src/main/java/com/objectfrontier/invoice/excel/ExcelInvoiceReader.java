package com.objectfrontier.invoice.excel;

import com.objectfrontier.invoice.excel.exception.ReportException;
import com.objectfrontier.invoice.excel.system.InvoiceUtil;
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

  private Utils utils;

  public ExcelInvoiceReader() {
    utils = Utils.getInstance();
    log.removeHandler(utils.getHandler());
    log.addHandler(utils.getHandler());
  }

  public Map<String, ClientAccount> parseAllClientInvoice(int year, InvoiceUtil.MONTH month) throws ReportException{
    if (year < 2015) throw new ReportException("Year to build sales report must be 2015 and above");
    this.reportingYear = year;
    this.reportingMonth = month;
    init();

    log("Collecting data for sales report");
    for(File file : invoiceFiles) {
      resetCurrent();
      if (!parseInvoiceFile(file)) {
        log("Something i did not expect, so could not process " + file.getAbsolutePath());
      }
    }

    return cache.clientAccountCache;
  }

  /**
   * Initialize method.  Currently performs cleanup of cache and reset local variables
   *
   * @throws ReportException
   */
  private void init() throws ReportException {
    resetCurrentRow();
    cache.clientAccountCache.clear();
    cache.projectCache.clear();
    currentProject = null;
    currentClientAccount = null;
    invoiceFiles = Utils.getInstance().getInvoiceFiles();
  }

  private void resetCurrent() {
    resetCurrentRow();
    currentClientAccount = null;
    currentProject = null;
  }

  private void resetCurrentRow() {
    currentRow = 0;
  }

  private String getSettingsSheetName() {
    return "Settings";
  }

  private String getReportingMonthSheetName() {
    return reportingMonth.toString() + reportingYear;
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

  private void addClient(String clientName) {
    log("Total Row Count is " + (getLastRowIndex() +1));
    log("I'm caching " + clientName + " details so I can work efficiently for you!!!");
    currentClientAccount = cache.addClient(clientName);
    currentClientAccount.code = getString(getNextRow(), CLIENT_CODE_COL_INDEX);
  }

  private void addProject(String code){
    log("Adding project to cache");
    currentProject = cache.addProject(code);
    currentProject.id = getString(getCurrentRow(), SOW_ID_COL_INDEX);
    currentProject.startDate = getDate(getCurrentRow(), SOW_START_DATE_COL_INDEX);
    currentProject.name = getString(getNextRow(), SOW_NAME_COL_INDEX);
    currentProject.endDate = getDate(getCurrentRow(), SOW_END_DATE_COL_INDEX);
    currentProject.rate = getRate();
    currentClientAccount.projects.add(currentProject);
  }

  private Employee getEmployee() {
    try {
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
      return employee;
    } catch (Exception exception) {
      log("\nWARNING: " + utils.getStackTrace(exception));
      log("Ignoring above error");
      //currently since no standards are followed there are rows containing employee name but not other
      // details for such data we will skip adding that employee
      return null;
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
    log("Fetching Row = " + (currentRow + 1));
    return row;
  }

  private boolean parseInvoiceFile(File invoiceFile) {
    FileInputStream fileInputStream = loadInvoice(invoiceFile);
    currentFile = invoiceFile;
    if (fileInputStream == null) return false;
    log("Started processing invoice sheet");
    loadWorkbook(fileInputStream);
    if (workbook == null) return false;
    loadSheets();
    if(reportingMonthSheet == null || settingsSheet ==null) return false;
    parseClientAccount();
    return true;
  }

  private void parseClientAccount() {
    while(currentRow <= getLastRowIndex()) {
      try {
        if (!columnHasMatchingString(CLIENT_NAME_LABEL_COL_INDEX, CLIENT_NAME_LABEL)) {
          getNextRow();
          continue;
        }
      } catch (Exception ex) {
        log(ex);
        log("Ignoring above exception and continuing with next row data");
        getNextRow();
        continue;
      }
      log("I found client details");
      String clientName = getString(getCurrentRow(), CLIENT_NAME_COL_INDEX);
      currentClientAccount = cache.getClient(clientName);
      if (currentClientAccount == null) {
        addClient(clientName);
      }
      parseProjects();
    }
  }

  private void parseProjects() {
    log("I'm about to parse the project section");
    while(currentRow <= getLastRowIndex()) {
      try {
        if (columnHasMatchingString(SOW_ID_LABEL_COL_INDEX, SOW_ID_LABEL)) {
          skipRows(1);
          String code = getString(getCurrentRow(), SOW_CODE_COL_INDEX);
          currentProject = cache.getProject(code);
          rewind(1);
          if (currentProject == null) {
            addProject(code);
          }

          // This skips rows till we identify Role column, to begin reading employee details
          if (skipTillColumnHasMatchingString(RESOURCE_ROLE_COL_INDEX, ROLE_LABEL)) {
            getNextRow();
            parseEmployees();
          }
        }

        if (columnHasMatchingString(SHADOW_RESOURCE_LABEL_COL_INDEX, SHADOW_RESOURCE_LABEL)) {
          parseShadowResources();
        }
        getNextRow();
      } catch (Exception e) {
        log(e);
        log("Ignoring above exception and continuing further");
        if (currentRow <= getLastRowIndex()) getNextRow();
      }
    }
  }

  private void parseEmployees() {
    log("I'm about to parse employees for project " + currentProject.code);
    while(currentRow <= getLastRowIndex()) {

      if (isThisRowContainingEmployeeForProject()) {
        Employee employee = getEmployee();
        if (employee != null) {
          currentProject.employees.add(employee);
          log(String.format("I found employee %s, %s, working for SOW/Project Code %s", employee.firstName,
                          employee.lastName,
                          currentProject.code));

        } else {
          log("WARNING: Employee name was found in row " + (currentRow + 1) + " but few other details were missing so ignoring");
        }
      } else if (isItEndOfThisProjectSection()) {
        log ("I finished collecting employees working in " + currentProject.code);
        return;
      }
      getNextRow();
    }
  }

  private void parseShadowResources() {
    log("Reading all shadow resources now.  I am amost done...");
    while(currentRow <= getLastRowIndex()) {
      if (isThisRowContainingEmployeeForProject()) {
        Employee employee = getEmployee();
        if (employee == null) {
          log("WARNING: I did not see all details for shadow resource in row " + (currentRow +1));
          getNextRow();
          continue;
        }
        employee.shadow = true;
        String code = getString(getCurrentRow(), SHADOW_RESOURCE_SOW_CODE_COL_INDEX);
        Project project = cache.getProject(code);
        if (project != null) {
          log("Adding shadow resource to project employee list");
          project.employees.add(employee);
        } else {
          log("WARNING: No valid project assigned to shadow resource , hence not adding " + employee.firstName + " "
                          + employee.lastName);
        }
      }
      getNextRow();
    }

  }

  private boolean isThisRowContainingEmployeeForProject() {
    log(String.format("Checking if the row %d contains employee details", (currentRow+1)));
    String fName;
    String lName;
    try {
      fName = getString(getCurrentRow(), RESOURCE_FIRST_NAME_COL_INDEX);
      lName = getString(getCurrentRow(), RESOURCE_LAST_NAME_COL_INDEX);
    } catch (Exception e) {
      log(e);
      log("Ignoring above exception");
      log("I did not find any employee first name and last name in row " + (currentRow+1));
      return false;
    }
    boolean isEmployeeRow = (fName != null & lName != null && fName.trim().length() > 0 && lName.trim().length() > 0);
    log("Current row "  + (isEmployeeRow ? "is " : "is NOT ") + "having employee details");
    return isEmployeeRow;
  }

  private boolean isItEndOfThisProjectSection() {
    log("Verifying if SOW section has come to an end");
    try {
      if (SOW_TOTAL_LABEL.equals(getString(getCurrentRow(), SOW_TOTAL_LABEL_COL_INDEX))) {
        log("End of sow section for project code " + currentProject.code);
        return true;
      }
    } catch (Exception e) {
      log(e);
      log("Ignoring above exception");
      log("I did not find employee details nor the end of current project section in row " + (currentRow +1));
    }
    return false;
  }

  private boolean skipTillColumnHasMatchingString(int colIndex, String expected) {
    log(String.format("I will skip rows until I find %s in column %d", expected, colIndex));
    while(currentRow <= getLastRowIndex()) {
      try {
        String value = getString(getNextRow(), colIndex);
        log(String.format("I will finish if column %d has %s.  I found %s", colIndex, expected, value));
        if (value != null && value.equals(expected)) {
          return true;
        }
      } catch (Exception ex) {
        log(ex);
        log("Ignoring above exception");
        //do nothing but keep proceeding
      }
    }
    log("I did not find matching value so I skipped all the way till end of the invoice file.");
    return false;
  }

  private boolean columnHasMatchingString(int colIndex, String expected) {
    try {
      String value = getString(getCurrentRow(), colIndex);
      log(String.format("Verifying if column %d has %s,  value I read is %s", colIndex, expected, value));
      return (value != null && value.equals(expected));
    } catch (Exception ex) {
      log(ex);
      log("WARNING: Unexpected thing happend when finding matching string in column " + colIndex);
      log("Ignoring above exception and proceeding further");
      return false;
    }
  }

  private XSSFRow getCurrentRow() {
    XSSFRow row = reportingMonthSheet.getRow(currentRow);
    log("Current Row = " + (currentRow +1));//+ " row data is " + row);
    return row;
  }

  private Rate getRate() {
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
