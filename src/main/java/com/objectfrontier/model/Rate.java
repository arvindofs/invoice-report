package com.objectfrontier.model;

import com.google.gson.GsonBuilder;

import java.util.HashMap;

/**
 * Created by ahariharan on 6/19/15.
 */
public class Rate extends HashMap<String, Double>{

  @Override public String toString() {
    return new GsonBuilder().setPrettyPrinting().create().toJson(this);
  }
}
