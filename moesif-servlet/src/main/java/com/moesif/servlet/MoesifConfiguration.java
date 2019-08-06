package com.moesif.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.moesif.api.models.EventModel;

public class MoesifConfiguration {

  public String identifyUser(HttpServletRequest request, HttpServletResponse response) {
    try {
      if (request.getUserPrincipal() == null) {
        return null;
      }
      return request.getUserPrincipal().getName();
    } catch (Exception e) {
      return null;
    }
  }

  public String identifyCompany(HttpServletRequest request, HttpServletResponse response) {
    return null;
  }

  public String getSessionToken(HttpServletRequest request, HttpServletResponse response) {
    return null;
  }

  public String getApiVersion(HttpServletRequest request, HttpServletResponse response) {
    return null;
  }

  public Object getMetadata(HttpServletRequest request, HttpServletResponse response) {
    return null;
  }

  public boolean skip(HttpServletRequest request, HttpServletResponse response) {
    return false;
  }

  public EventModel maskContent(EventModel eventModel) {
    return eventModel;
  }

  public boolean disableTransactionId = false;

  @Deprecated
  public String getTags(HttpServletRequest request, HttpServletResponse response) {
    return null;
  }
}