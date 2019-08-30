package com.moesif.springrequestexample;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import com.moesif.springrequest.MoesifRequestConfiguration;

public class RequestConfig extends MoesifRequestConfiguration {
  @Override
  public String identifyUser(HttpRequest request, ClientHttpResponse response) {
    return "my_user_id";
  }
  
  @Override
  public String identifyCompany(HttpRequest request, ClientHttpResponse response) {
    return "my_company_id";
  }
}