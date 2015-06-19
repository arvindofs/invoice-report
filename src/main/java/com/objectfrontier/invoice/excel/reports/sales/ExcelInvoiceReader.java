package com.objectfrontier.invoice.excel.reports.sales;

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
  private Logger log = Logger.getLogger(this.getClass().getName());

  private DataCache cache = new DataCache();

  private XSSFWorkbook workbook;
  private XSSFSheet reportingMonthSheet;
  private XSSFSheet settingsSheet;

  private int reportingYear;
  private InvoiceUtil.MONTH reportingMonth;

  private int currentRow;

  private ClientAccount currentClientAccount;
  private Project currentProject;


  public ExcelInvoiceReader() {
    init();
  }

  private void init() {
    resetCurrentRow();
    invoiceFiles = Utils.getInstance().getInvoiceFiles();
  }

  public Map<String, ClientAccount> buildSalesReport(int year, InvoiceUtil.MONTH month) throws ReportException{
    if (year < 2015) throw new ReportException("Year to build sales report must be 2015 and above");
    this.reportingYear = year;
    this.reportingMonth = month;

    log.info("Sales report will be generated for " + getReportingMonthSheetName());
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
      log.info("Could not process file " + invoiceFile.getAbsolutePath());
      log.info(e.getMessage());
    }
    return null;
  }

  private void loadWorkbook(FileInputStream fileInputStream) {
    try {
      workbook = new XSSFWorkbook(fileInputStream);
    } catch (IOException e) {
      log.info("Error loading workbook " + e.getMessage());
      workbook = null;
    }
  }

  private void loadSheets() {
    settingsSheet = workbook.getSheet(getSettingsSheetName());
    reportingMonthSheet = workbook.getSheet(getReportingMonthSheetName());
  }

  private void processInvoice(File invoiceFile) {
    FileInputStream fileInputStream = loadInvoice(invoiceFile);
    if (fileInputStream == null) return;
    log.info("Beggining to process " + invoiceFile.getAbsolutePath());
    loadWorkbook(fileInputStream);
    if (workbook == null) return;
    loadSheets();
    if(reportingMonthSheet == null || settingsSheet ==null) return;
    try {
      readClientAccount();
    }  catch (ReportException exception) {
      log.info("Error occured during report generation for " + invoiceFile.getAbsolutePath());
      log.info("Root cause: " + exception.getMessage());
    }
  }

  private void readClientAccount() throws ReportException {
    if (!getString(getCurrentRow(), CLIENT_NAME_LABEL_COL_INDEX).equals(CLIENT_NAME_LABEL))
      throw new ReportException("Monthly invoice sheet is not in valid format.");

    String clientName = getString(getCurrentRow(), CLIENT_NAME_COL_INDEX);
    currentClientAccount = cache.getClient(clientName);
    if (currentClientAccount == null) {
      addClient(clientName);
    } else {
      skipRows(1);
    }
    skipRows(4);
    readProjectInvoice();
//    log.info(currentClientAccount.toString());
  }

  private void addClient(String clientName) {
    currentClientAccount = cache.addClient(clientName);
    currentClientAccount.code = getString(getNextRow(), CLIENT_CODE_COL_INDEX);
  }

  private void readProjectInvoice() throws ReportException {
    while (currentRow < getLastRowIndex()-1) {
      if (getCurrentRow() == null) break;

      if (SOW_ID_LABEL.equals(getString(getCurrentRow(), SOW_ID_LABEL_COL_INDEX))) {
        skipRows(1);
        String code = getString(getCurrentRow(), SOW_CODE_COL_INDEX);
        currentProject = cache.getProject(code);
        rewind(1);
        if (currentProject == null) {
          addProject(code);
        } else {
          skipRows(0);
        }
        skipRows(4);
        addEmployees();
      }

      getNextRow();

      if (SHADOW_RESOURCE_LABEL.equals(getString(getCurrentRow(), SHADOW_RESOURCE_LABEL_COL_INDEX))) {
        addShadowResources();
      }

    }
  }

  private void addProject(String code) throws ReportException {
    currentProject = cache.addProject(code);
    currentProject.id = getString(getCurrentRow(), SOW_ID_COL_INDEX);
    currentProject.startDate = getDate(getCurrentRow(), SOW_START_DATE_COL_INDEX);
    currentProject.name = getString(getNextRow(), SOW_NAME_COL_INDEX);
    currentProject.endDate = getDate(getCurrentRow(), SOW_END_DATE_COL_INDEX);
    currentProject.rate = getRate();
    currentClientAccount.projects.add(currentProject);
  }

  private void addEmployees() throws ReportException {
    while (currentRow < getLastRowIndex() -1) {
      currentProject.employees.add(getEmployee());
      if (SOW_TOTAL_LABEL.equals(getString(getNextRow(), SOW_TOTAL_LABEL_COL_INDEX))) break;
    }
    skipRows(2);
  }

  private Employee getEmployee() throws ReportException{
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
    billing.rate = getRate().get(employee.location).doubleValue();
    billing.workLocation = employee.location;
    employee.billing = billing;

    return employee;
  }

  private void addShadowResources() throws ReportException {
    skipRows(1);
    while (currentRow < getLastRowIndex() -1) {
      Employee employee = getEmployee();
      employee.shadow = true;
      String startRef = getCellReference(getCurrentRow(), RESOURCE_START_DATE_COL_INDEX);
      String endRef = getCellReference(getCurrentRow(), RESOURCE_END_DATE_COL_INDEX);
      String projCode = getString(getCurrentRow(), SHADOW_RESOURCE_SOW_CODE_COL_INDEX); //getCurrentRow().getCell(RESOURCE_NUM_DAYS_WORKED_COL_INDEX).getRawValue()
      Project project = cache.getProject(projCode);
      if (project != null) {
        project.employees.add(employee);
      }
      if (SOW_TOTAL_LABEL.equals(getString(getNextRow(), SOW_TOTAL_LABEL_COL_INDEX))) break;
      if (getCurrentRow().getCell(1) == null) break;
    }

  }

  private void skipRows(int count) {
    currentRow += count + 1;
  }

  private void rewind(int count) {
    currentRow -= (count + 1);
  }

  private XSSFRow getPreviousRow() {
    return reportingMonthSheet.getRow(--currentRow);
  }

  private XSSFRow getNextRow() {
    return reportingMonthSheet.getRow(++currentRow);
  }

  private XSSFRow getCurrentRow() {
    return reportingMonthSheet.getRow(currentRow);
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

  private String getFormula(XSSFRow row, int colIndex) {
    XSSFCell cell = row.getCell(colIndex);
    return cell.getCellFormula();
  }

  private String setFormulaResultingString(XSSFRow row, int colIndex, String formula) {
    XSSFCell cell = setFormula(row, colIndex, formula);
    return cell.getStringCellValue();
  }

  private double setFormulaResultingNumeric(XSSFRow row, int colIndex, String formula) {
    XSSFCell cell = setFormula(row, colIndex, formula);
    return cell.getNumericCellValue();
  }

  private Date setFormulaResultingDate(XSSFRow row, int colIndex, String formula) {
    XSSFCell cell = setFormula(row, colIndex, formula);
    return cell.getDateCellValue();
  }

  private XSSFCell setFormula(XSSFRow row, int colIndex, String formula) {
    XSSFCell cell = row.getCell(colIndex);
    cell.setCellFormula(formula);
    return cell;
  }

  private String getCellReference(XSSFRow row, int column) {
    return row.getCell(column).getReference();
  }

  private String getCellReference(XSSFSheet sheet, int row, int column) {
    return sheet.getRow(row).getCell(column).getReference();
  }
}
