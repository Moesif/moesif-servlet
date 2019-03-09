package com.moesif.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.moesif.api.models.EventModel;

public class MoesifConfigurationAdapter implements MoesifConfiguration {

  public boolean skip(HttpServletRequest request, HttpServletResponse response) {
    return false;
  }

  public EventModel maskContent(EventModel eventModel) {
    return eventModel;
  }

  public String identifyUser(HttpServletRequest request, HttpServletResponse response) {
    return null;
  }

  public String getSessionToken(HttpServletRequest request, HttpServletResponse response) {
    return null;
  }

  public String getTags(HttpServletRequest request, HttpServletResponse response) {
    return null;
  }

  public String getApiVersion(HttpServletRequest request, HttpServletResponse response) {
    return null;
  }
  
  public boolean disableTransactionId() {
	  return false;
  }
}