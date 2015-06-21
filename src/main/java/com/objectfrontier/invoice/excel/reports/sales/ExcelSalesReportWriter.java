package com.objectfrontier.invoice.excel.reports.sales;

import com.objectfrontier.invoice.excel.exception.ReportException;
import com.objectfrontier.invoice.excel.system.InvoiceUtil.MONTH;
import com.objectfrontier.model.ClientAccount;
import com.objectfrontier.model.Employee;
import com.objectfrontier.model.Project;
import org.apache.poi.xssf.usermodel.*;

import java.util.Iterator;
import java.util.Map;

/**
 * Created by ahariharan on 6/19/15.
 */
public class ExcelSalesReportWriter {

  private Map<String, ClientAccount> clientAccounts;
  private XSSFWorkbook workbook;
  private XSSFSheet sheet;

  private int reportingYear;
  private MONTH reportingMonth;
  int currentRowNum;
  int currentColumnNum;

  private ClientAccount currentClientAccount;
  private Project currentProject;
  private Employee currentEmployee;

  // Sl No	First Name 	Last Name	Role	Location	Client Name	Project Code	Invoice Rate/Resource	# of days worked	Invoice Amount	Shadow Resource

  private enum DataType {NUMERIC, STRING, DATE, BOOLEAN}

  private String[] columns = new String[] { "Sl No", "First Name", "Last Name", "Role", "Work Location", "Client Name",
                  "Project Code", "Invoice Rate /Resource", "Business Days Worked", "Invoiced Amount",
                  "Shadow Resource", "Business Start Date of Month", "Business End Date of Month" };

  private DataType[] columntype = new DataType[] { DataType.NUMERIC, DataType.STRING, DataType.STRING, DataType.STRING,
                  DataType.STRING, DataType.STRING, DataType.STRING, DataType.NUMERIC, DataType.NUMERIC,
                  DataType.NUMERIC, DataType.BOOLEAN, DataType.DATE, DataType.DATE };

  public ExcelSalesReportWriter(Map<String, ClientAccount> clientAccounts) {
    this.clientAccounts = clientAccounts;
  }

  private String getReportingMonthSheetName() {
    return reportingMonth.toString() + reportingYear;
  }

  private void init(XSSFWorkbook existingWorkbook) throws ReportException {
    if (clientAccounts == null || clientAccounts.isEmpty())
      throw new ReportException("No data available to generate report");

    this.workbook = (existingWorkbook == null) ? new XSSFWorkbook() : existingWorkbook;
    sheet = this.workbook.getSheet(getReportingMonthSheetName());
    if (sheet != null) {
      this.workbook.removeSheetAt(existingWorkbook.getSheetIndex(sheet));
    }
    sheet = this.workbook.createSheet(getReportingMonthSheetName());

    writeHeader();
  }

  private void writeHeader() {

    XSSFRow row = sheet.createRow(thisRow());
    for(String columnName : columns) {
      XSSFCell cell = row.createCell(thisColumn()) ;
//      headerStyle(cell);
      cell.setCellValue(columnName);
    }
  }

  private void headerStyle (XSSFCell cell) {
    XSSFCellStyle cellStyle = cell.getCellStyle();
    cellStyle.setAlignment(XSSFCellStyle.ALIGN_CENTER);
    XSSFFont font = new XSSFFont(cellStyle.getFont().getCTFont(), cellStyle.getFont().getIndex());
    font.setBold(true);
    cellStyle.setFont(font);
  }

  private int thisRow() {
    return currentRowNum++;
  }

  private int thisColumn() {
    if (currentColumnNum + 1   == columns.length) {
      currentColumnNum = 0;
      return columns.length - 1;
    }
    return currentColumnNum++;
  }

  public XSSFWorkbook getSalesReport(XSSFWorkbook workbook, int year, MONTH month) throws ReportException {
    this.reportingYear = year;
    this.reportingMonth = month;
    init(workbook);
    Iterator iterator = clientAccounts.keySet().iterator();
    while(iterator.hasNext()) {
      currentClientAccount = clientAccounts.get(iterator.next());
      writeClientSales();
    }

    return this.workbook;
  }

  private void writeClientSales() throws ReportException {
    if (currentClientAccount == null) throw new ReportException("Missing client account data");
    for(Project project : currentClientAccount.projects) {
      this.currentProject = project;
      writeProjectSales();
    }
  }

  private void writeProjectSales() {
    for(Employee employee : currentProject.employees) {
      this.currentEmployee = employee;
      writeDetails();
    }
  }

  private void writeDetails() {
    XSSFRow row = sheet.createRow(thisRow());
    createCell(row).setCellValue(currentRowNum-1);
    createCell(row).setCellValue(currentEmployee.firstName);
    createCell(row).setCellValue(currentEmployee.lastName);
    createCell(row).setCellValue(currentEmployee.role);
    createCell(row).setCellValue(currentEmployee.location);
    createCell(row).setCellValue(currentClientAccount.name);
    createCell(row).setCellValue(currentProject.code);
    createCell(row).setCellValue(currentEmployee.billing.rate);
    createCell(row).setCellValue(currentEmployee.billing.billableDays);
    createCell(row).setCellValue(currentEmployee.billing.billableDays);
    createCell(row).setCellValue(currentEmployee.shadow ? "Yes" : "No");
    createCell(row).setCellValue(currentEmployee.billing.startDate);
    createCell(row).setCellValue(currentEmployee.billing.endDate);

  }

  private XSSFCell createCell(XSSFRow row) {
    return row.createCell(thisColumn());
  }
}
