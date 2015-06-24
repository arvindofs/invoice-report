package com.objectfrontier.model;

import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ahariharan on 6/18/15.
 */
public class Employee {
  public long id;
  public String firstName;
  public String lastName;
  public String role;
  public Billing billing;
  public boolean shadow;
  public String location;
  public List<String> assignedProjectCodes;
  public List<Long> directReportingEmployeeIds;
  public long managerId;

  public Employee() {
    directReportingEmployeeIds = new ArrayList();
    assignedProjectCodes = new ArrayList();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    Employee employee = (Employee) o;

    if (id != employee.id)
      return false;
    if (shadow != employee.shadow)
      return false;
    if (managerId != employee.managerId)
      return false;
    if (firstName != null ? !firstName.equals(employee.firstName) : employee.firstName != null)
      return false;
    if (lastName != null ? !lastName.equals(employee.lastName) : employee.lastName != null)
      return false;
    if (role != null ? !role.equals(employee.role) : employee.role != null)
      return false;
    if (billing != null ? !billing.equals(employee.billing) : employee.billing != null)
      return false;
    if (location != null ? !location.equals(employee.location) : employee.location != null)
      return false;
    if ((assignedProjectCodes != null) ?
                    !assignedProjectCodes.equals(employee.assignedProjectCodes) :
                    (employee.assignedProjectCodes != null)) {
      return false;
    }
    return !(directReportingEmployeeIds != null ?
                    !directReportingEmployeeIds.equals(employee.directReportingEmployeeIds) :
                    employee.directReportingEmployeeIds != null);

  }

  @Override
  public int hashCode() {
    int result = (int) (id ^ (id >>> 32));
    result = 31 * result + (firstName != null ? firstName.hashCode() : 0);
    result = 31 * result + (lastName != null ? lastName.hashCode() : 0);
    result = 31 * result + (role != null ? role.hashCode() : 0);
    result = 31 * result + (billing != null ? billing.hashCode() : 0);
    result = 31 * result + (shadow ? 1 : 0);
    result = 31 * result + (location != null ? location.hashCode() : 0);
    result = 31 * result + (assignedProjectCodes != null ? assignedProjectCodes.hashCode() : 0);
    result = 31 * result + (directReportingEmployeeIds != null ? directReportingEmployeeIds.hashCode() : 0);
    result = 31 * result + (int) (managerId ^ (managerId >>> 32));
    return result;
  }

  @Override
  public String toString() {
    return new GsonBuilder().setPrettyPrinting().create().toJson(this);
  }
}
