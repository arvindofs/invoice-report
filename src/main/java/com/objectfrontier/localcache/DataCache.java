package com.objectfrontier.localcache;

import com.objectfrontier.model.ClientAccount;
import com.objectfrontier.model.Employee;
import com.objectfrontier.model.Project;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Created by ahariharan on 6/18/15.
 */
public class DataCache {


  Map<String, ClientAccount> clientAccountCache = Collections.synchronizedMap(new WeakHashMap<String, ClientAccount>());
//  Map<String, Employee> employeeCache = Collections.synchronizedMap(new WeakHashMap<String, Employee>());
  Map<String, Project> projectCache = Collections.synchronizedMap(new WeakHashMap<String, Project>());

  public ClientAccount getClient(String name) {
    return clientAccountCache.get(name);
  }

  public ClientAccount addClient(String  name) {
    ClientAccount clientAccount = new ClientAccount(name);
    clientAccountCache.put(clientAccount.name, clientAccount);
    return clientAccount;
  }

//  public Employee getEmployee(String firstName, String lastName) {
//    return employeeCache.get(String.format("%s.%s", firstName, lastName));
//  }
//
//  public Employee addEmployee(String firstName, String lastName) {
//    Employee employee = new Employee(firstName, lastName);
//    employeeCache.put(String.format("%s.%s", employee.firstName, employee.lastName), employee);
//    return employee;
//  }

  public Project getProject(String name) {
    return projectCache.get(name);
  }

  public Project addProject(String code) {
    Project project = new Project(code);
    projectCache.put(code, project);
    return project;
  }
}
