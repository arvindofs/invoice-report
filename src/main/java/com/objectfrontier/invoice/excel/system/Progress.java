package com.objectfrontier.invoice.excel.system;

/**
 * Created by ahariharan on 6/21/15.
 */
public class Progress {
  private static final Progress progress = new Progress();

  private int limit = 0;
  private int current = -100;

  public static Progress instance() {
    return progress;
  }

  public void addActivity(){
    limit++;
  }

  public void activityDone() {
    if (current == -100) current=0;
    current++;
  }

  public int getLimit() {
    return limit;
  }

  public int getCurrent() {
    return current;
  }

  public void reset() {
    limit = 0;
    current = -100;

  }


}
