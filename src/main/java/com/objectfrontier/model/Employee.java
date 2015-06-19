package com.objectfrontier.model;

import java.util.List;

/**
 * Created by ahariharan on 6/18/15.
 */
public class Employee {
  public String firstName;
  public String lastName;
  public String role;
  public List<Project> projects;
  public Billing billing;
  public boolean shadow;

  public Employee(String firstName, String lastName) {
    this.firstName = firstName;
    this.lastName = lastName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    Employee employee = (Employee) o;

    if (shadow != employee.shadow)
      return false;
    if (billing != null ? !billing.equals(employee.billing) : employee.billing != null)
      return false;
    if (firstName != null ? !firstName.equals(employee.firstName) : employee.firstName != null)
      return false;
    if (lastName != null ? !lastName.equals(employee.lastName) : employee.lastName != null)
      return false;
    if (projects != null ? !projects.equals(employee.projects) : employee.projects != null)
      return false;
    if (role != null ? !role.equals(employee.role) : employee.role != null)
      return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = firstName != null ? firstName.hashCode() : 0;
    result = 31 * result + (lastName != null ? lastName.hashCode() : 0);
    result = 31 * result + (role != null ? role.hashCode() : 0);
    result = 31 * result + (projects != null ? projects.hashCode() : 0);
    result = 31 * result + (billing != null ? billing.hashCode() : 0);
    result = 31 * result + (shadow ? 1 : 0);
    return result;
  }

  @Override public String toString() {
    return "Employee{" +
                    "firstName='" + firstName + '\'' +
                    ", lastName='" + lastName + '\'' +
                    ", role='" + role + '\'' +
                    ", projects=" + projects +
                    ", billing=" + billing +
                    ", shadow=" + shadow +
                    '}';
  }
}
