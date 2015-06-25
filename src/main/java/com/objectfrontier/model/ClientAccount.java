package com.objectfrontier.model;

import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ahariharan on 6/18/15.
 */
public class ClientAccount {
  public String name;
  public String code;
  public List<Project> projects = new ArrayList();
  public Employee clientOwner;
  public Employee accountOwner;

  public ClientAccount(String name) {
    this.name = name;
  }

  public float getTotalInvoiceAmount() {
    float totalInvoiceAmount = 0;
    for(Project project : projects) {
      totalInvoiceAmount += project.getTotalInvoiceAmount();
    }
    return totalInvoiceAmount;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    ClientAccount that = (ClientAccount) o;

    if (name != null ? !name.equals(that.name) : that.name != null)
      return false;
    if (code != null ? !code.equals(that.code) : that.code != null)
      return false;
    if (projects != null ? !projects.equals(that.projects) : that.projects != null)
      return false;
    if (clientOwner != null ? !clientOwner.equals(that.clientOwner) : that.clientOwner != null)
      return false;
    return !(accountOwner != null ? !accountOwner.equals(that.accountOwner) : that.accountOwner != null);

  }

  @Override
  public int hashCode() {
    int result = name != null ? name.hashCode() : 0;
    result = 31 * result + (code != null ? code.hashCode() : 0);
    result = 31 * result + (projects != null ? projects.hashCode() : 0);
    result = 31 * result + (clientOwner != null ? clientOwner.hashCode() : 0);
    result = 31 * result + (accountOwner != null ? accountOwner.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return new GsonBuilder().setPrettyPrinting().create().toJson(this);
  }
}
