package com.moesif.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.moesif.api.models.EventModel;

public interface MoesifConfiguration {

  boolean skip(HttpServletRequest request, HttpServletResponse response);

  EventModel maskContent(EventModel eventModel);

  String identifyUser(HttpServletRequest request, HttpServletResponse response);

  String getSessionToken(HttpServletRequest request, HttpServletResponse response);

  String getTags(HttpServletRequest request, HttpServletResponse response);

  String getApiVersion(HttpServletRequest request, HttpServletResponse response);
  
  boolean disableTransactionId();
}