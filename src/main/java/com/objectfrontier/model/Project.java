package com.objectfrontier.model;

import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by ahariharan on 6/18/15.
 */
public class Project {
  public String id;
  public String name;
  public String code;
  public Date startDate;
  public Date endDate;
  public List<Employee> employees = new ArrayList();
  public Employee deliveryOwner;
  public Employee podOwner;
  public Rate rate;

  public Project(String code) {
    this.code = code;
  }

  public float getTotalInvoiceAmount() {
    float totalInvoiceAmount = 0;
    for (Employee employee : employees) {
      totalInvoiceAmount += employee.billing.billed;
    }
    return totalInvoiceAmount;
  }

  @Override public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Project project = (Project) o;

    if (id != null ? !id.equals(project.id) : project.id != null)
      return false;
    if (name != null ? !name.equals(project.name) : project.name != null)
      return false;
    if (code != null ? !code.equals(project.code) : project.code != null)
      return false;
    if (startDate != null ? !startDate.equals(project.startDate) : project.startDate != null)
      return false;
    if (endDate != null ? !endDate.equals(project.endDate) : project.endDate != null)
      return false;
    if (employees != null ? !employees.equals(project.employees) : project.employees != null)
      return false;
    if (deliveryOwner != null ? !deliveryOwner.equals(project.deliveryOwner) : project.deliveryOwner != null)
      return false;
    if (podOwner != null ? !podOwner.equals(project.podOwner) : project.podOwner != null)
      return false;
    return !(rate != null ? !rate.equals(project.rate) : project.rate != null);

  }

  @Override
  public int hashCode() {
    int result = id != null ? id.hashCode() : 0;
    result = 31 * result + (name != null ? name.hashCode() : 0);
    result = 31 * result + (code != null ? code.hashCode() : 0);
    result = 31 * result + (startDate != null ? startDate.hashCode() : 0);
    result = 31 * result + (endDate != null ? endDate.hashCode() : 0);
    result = 31 * result + (employees != null ? employees.hashCode() : 0);
    result = 31 * result + (deliveryOwner != null ? deliveryOwner.hashCode() : 0);
    result = 31 * result + (podOwner != null ? podOwner.hashCode() : 0);
    result = 31 * result + (rate != null ? rate.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return new GsonBuilder().setPrettyPrinting().create().toJson(this);
  }
}
