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
  public Rate rate;

  public Project(String code) {
    this.code = code;
  }

  public float getTotalInvoiceAmount() throws Exception{
    throw new Exception("Method not implemented");
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    Project project = (Project) o;

    if (code != null ? !code.equals(project.code) : project.code != null)
      return false;
    if (employees != null ? !employees.equals(project.employees) : project.employees != null)
      return false;
    if (endDate != null ? !endDate.equals(project.endDate) : project.endDate != null)
      return false;
    if (id != null ? !id.equals(project.id) : project.id != null)
      return false;
    if (name != null ? !name.equals(project.name) : project.name != null)
      return false;
    if (rate != null ? !rate.equals(project.rate) : project.rate != null)
      return false;
    if (startDate != null ? !startDate.equals(project.startDate) : project.startDate != null)
      return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = id != null ? id.hashCode() : 0;
    result = 31 * result + (name != null ? name.hashCode() : 0);
    result = 31 * result + (code != null ? code.hashCode() : 0);
    result = 31 * result + (startDate != null ? startDate.hashCode() : 0);
    result = 31 * result + (endDate != null ? endDate.hashCode() : 0);
    result = 31 * result + (employees != null ? employees.hashCode() : 0);
    result = 31 * result + (rate != null ? rate.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return new GsonBuilder().setPrettyPrinting().create().toJson(this);
  }
}
