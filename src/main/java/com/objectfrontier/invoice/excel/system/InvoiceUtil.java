package com.objectfrontier.invoice.excel.system;

/**
 * Created by ahariharan on 6/18/15.
 */
public class InvoiceUtil {

  public static final int CLIENT_NAME_LABEL_COL_INDEX = 2;
  public static final int CLIENT_NAME_COL_INDEX = 3;
  public static final int CLIENT_CODE_LABEL_COL_INDEX = 2;
  public static final int CLIENT_CODE_COL_INDEX = 3;

  public static final int SOW_LABEL_COL_INDEX = 2;
  public static final int SOW_COL_INDEX = 3;
  public static final int SOW_NAME_LABEL_COL_INDEX = 2;
  public static final int SOW_NAME_COL_INDEX = 3;
  public static final int SOW_START_DATE_LABEL_COL_INDEX = 5;
  public static final int SOW_START_DATE_COL_INDEX = 6;
  public static final int SOW_END_DATE_LABEL_COL_INDEX = 5;
  public static final int SOW_END_DATE_COL_INDEX = 6;
  public static final int SOW_CODE_LABEL_COL_INDEX = 11;
  public static final int SOW_CODE_COL_INDEX = 12;


  public static final int RESOURCE_ROLE_COL_INDEX = 2;
  public static final int RESOURCE_LOCATION_COL_INDEX = 3;
  public static final int RESOURCE_BILLABLE_PERCENT_COL_INDEX = 4;
  public static final int RESOURCE_PTO_COUNT_COL_INDEX = 5;
  public static final int RESOURCE_INVOICE_AMOUNT_COL_INDEX = 6;
  public static final int RESOURCE_START_DATE_COL_INDEX = 7;
  public static final int RESOURCE_END_DATE_COL_INDEX = 8;
  public static final int RESOURCE_NUM_DAYS_WORKED_COL_INDEX = 9;
  public static final int RESOURCE_FIRST_NAME_COL_INDEX = 11;
  public static final int RESOURCE_LAST_NAME_COL_INDEX = 12;

  public static final int SHADOW_RESOURCE_SOW_CODE_COL_INDEX = 10;


  public static final int SETTINGS_LOCATION_LABEL_COL_INDEX = 2;
  public static final int SETTINGS_LOCATION_RATE_COL_INDEX = 3;

  public static final String CLIENT_NAME_LABEL = "Client Name";
  public static final String CLIENT_CODE_LABEL = "Client Code";
  public static final String SOW_LABEL = "SOW #";
  public static final String SOW_NAME_LABEL = "SOW Name";
  public static final String SOW_TOTAL_LABEL = "SOW Total";
  public static final String ROLE_LABEL = "Role";
  public static final String CORE_RESOURCE_LABEL = "Core Resources";
  public static final String SHADOW_RESOURCE_LABEL = "shadow Resources";

  public static enum MONTH {Jan, Feb, Mar, Apr, May, Jun, Jul, Aug, Sep, Nov, Dec};

}
