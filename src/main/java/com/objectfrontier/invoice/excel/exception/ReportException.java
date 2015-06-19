package com.objectfrontier.invoice.excel.exception;

/**
 * Created by ahariharan on 6/19/15.
 */
public class ReportException extends Exception {
  public ReportException() {
  }

  public ReportException(String message) {
    super(message);
  }

  public ReportException(String message, Throwable cause) {
    super(message, cause);
  }

  public ReportException(Throwable cause) {
    super(cause);
  }

  public ReportException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
