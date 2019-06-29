package com.moesif.servlet.spring;
import javax.servlet.Filter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.moesif.api.models.EventModel;
import com.moesif.servlet.MoesifConfigurationAdapter;
import com.moesif.servlet.MoesifConfiguration;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.context.annotation.*;
import com.moesif.servlet.MoesifFilter;

@org.springframework.context.annotation.Configuration
public class MyConfig extends WebMvcConfigurerAdapter{

  @Bean
  public Filter moesifFilter() {

    MoesifConfiguration config = new MoesifConfiguration() {
      @Override
      public boolean skip(HttpServletRequest request, HttpServletResponse response) {
        // Skip logging health probes
        return request.getRequestURI().contains("health/probe");
      }

      @Override
      public EventModel maskContent(EventModel eventModel) {
        return eventModel;
      }

      @Override
      public String identifyUser(HttpServletRequest request, HttpServletResponse response) {
        if (request.getUserPrincipal() == null) {
          return null;
        }
        return request.getUserPrincipal().getName();
      }

      @Override
      public String getSessionToken(HttpServletRequest request, HttpServletResponse response) {
        return request.getHeader("Authorization");
      }

      @Override
      public String getTags(HttpServletRequest request, HttpServletResponse response) {
        return null;
      }

      @Override
      public String getApiVersion(HttpServletRequest request, HttpServletResponse response) {
        return request.getHeader("X-Api-Version");
      }
    };

    MoesifFilter moesifFilter = new MoesifFilter("Your Application Id", config, true);
    
    // Set flag to log request and response body
    moesifFilter.setLogBody(true);

    return moesifFilter;
  }
}
