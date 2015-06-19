package com.objectfrontier.invoice.excel.reports.sales;

import com.objectfrontier.model.ClientAccount;

import java.util.Map;

/**
 * Created by ahariharan on 6/19/15.
 */
public class ExcelSalesReportWriter {

  Map<String, ClientAccount> clients;

  public ExcelSalesReportWriter(Map<String, ClientAccount> clients) {
    this.clients = clients;
  }

}
