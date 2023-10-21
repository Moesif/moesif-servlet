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
    try {
      return request.getRequestedSessionId();
    } catch (Exception e) {
      return null;
    }
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
  public int batchSize = 100;         // batch size to chunk the queued events before transfer to moesif.
  public int batchMaxTime = 2;        // in seconds - time to send batch events periodically.
  public int queueSize = 1000000;        // maximum queue capacity to hold events.
  public int retry = 0;               // how many times to retry, if fails to post events.ß
  public int updateConfigTime = 5*60; // in seconds - time to update app config periodically.

  @Deprecated
  public String getTags(HttpServletRequest request, HttpServletResponse response) {
    return null;
  }
}