package com.objectfrontier.localcache;

import com.objectfrontier.model.ClientAccount;
import com.objectfrontier.model.Project;

import java.util.Collections;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Created by ahariharan on 6/18/15.
 */
public class DataCache {

  public SortedMap<String, ClientAccount> clientAccountCache = Collections.synchronizedSortedMap(
                  new TreeMap<String, ClientAccount>());
  public SortedMap<String, Project> projectCache = Collections.synchronizedSortedMap(new TreeMap<String, Project>());

  public ClientAccount getClient(String uniqueKey) {
    return clientAccountCache.get(uniqueKey);
  }

  public ClientAccount addClient(String  name) {
    ClientAccount clientAccount = new ClientAccount(name);
    clientAccountCache.put(clientAccount.name, clientAccount);
    return clientAccount;
  }

  public Project getProject(String uniqueKey) {
    return projectCache.get(uniqueKey);
  }

  public Project addProject(String uniqueKey) {
    Project project = new Project(uniqueKey);
    projectCache.put(uniqueKey, project);
    return project;
  }
}
