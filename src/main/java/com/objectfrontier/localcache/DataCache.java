package com.objectfrontier.localcache;

import com.objectfrontier.model.ClientAccount;
import com.objectfrontier.model.Employee;
import com.objectfrontier.model.Project;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Created by ahariharan on 6/18/15.
 */
public class DataCache {

  public Map<String, ClientAccount> clientAccountCache = Collections.synchronizedMap(new HashMap<String, ClientAccount>());
  public Map<String, Project> projectCache = Collections.synchronizedMap(new HashMap<String, Project>());

  public ClientAccount getClient(String name) {
    return clientAccountCache.get(name);
  }

  public ClientAccount addClient(String  name) {
    ClientAccount clientAccount = new ClientAccount(name);
    clientAccountCache.put(clientAccount.name, clientAccount);
    return clientAccount;
  }

  public Project getProject(String name) {
    return projectCache.get(name);
  }

  public Project addProject(String code) {
    Project project = new Project(code);
    projectCache.put(code, project);
    return project;
  }
}
