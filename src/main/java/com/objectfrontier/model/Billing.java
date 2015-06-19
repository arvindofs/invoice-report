package com.objectfrontier.model;

import com.google.gson.GsonBuilder;

import java.util.Date;

/**
 * Created by ahariharan on 6/18/15.
 */
public class Billing {
  public Date startDate;
  public Date endDate;
  public double rate;
  public double billed;
  public double billiablePercent;
  public double billableDays;
  public double ptoDays;
  public String workLocation;

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    Billing billing = (Billing) o;

    if (Double.compare(billing.billableDays, billableDays) != 0)
      return false;
    if (Double.compare(billing.billed, billed) != 0)
      return false;
    if (Double.compare(billing.billiablePercent, billiablePercent) != 0)
      return false;
    if (Double.compare(billing.ptoDays, ptoDays) != 0)
      return false;
    if (Double.compare(billing.rate, rate) != 0)
      return false;
    if (endDate != null ? !endDate.equals(billing.endDate) : billing.endDate != null)
      return false;
    if (startDate != null ? !startDate.equals(billing.startDate) : billing.startDate != null)
      return false;
    if (workLocation != null ? !workLocation.equals(billing.workLocation) : billing.workLocation != null)
      return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result;
    long temp;
    result = startDate != null ? startDate.hashCode() : 0;
    result = 31 * result + (endDate != null ? endDate.hashCode() : 0);
    temp = Double.doubleToLongBits(rate);
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(billed);
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(billiablePercent);
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(billableDays);
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(ptoDays);
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    result = 31 * result + (workLocation != null ? workLocation.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return new GsonBuilder().setPrettyPrinting().create().toJson(this);
  }
}
