package org.jzkit.service.z3950server;

import org.jzkit.search.LandscapeSpecification;
import org.jzkit.search.util.QueryModel.QueryModel;

public class ZSetInfo {

  public String setname = null;
  public LandscapeSpecification landscape = null;
  public QueryModel query_model = null;

  public ZSetInfo() {
  }

  public ZSetInfo(String setname,
                  QueryModel query_model,
                  LandscapeSpecification landscape) {
    this.setname = setname;
    this.landscape = landscape;
    this.query_model = query_model;
  }

  public String getSetname() {
    return setname;
  }

  public QueryModel getQueryModel() {
    return query_model;
  }

  public LandscapeSpecification getLandscape() {
    return landscape;
  }
}
