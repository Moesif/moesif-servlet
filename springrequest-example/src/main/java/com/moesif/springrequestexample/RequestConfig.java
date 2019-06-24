package com.moesif.springrequestexample;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import com.moesif.springrequest.MoesifRequestConfiguration;

public class RequestConfig extends MoesifRequestConfiguration {
  @Override
  public String identifyUser(HttpRequest request, ClientHttpResponse response) {
    return "test_user";
  }
  
  @Override
  public String identifyCompany(HttpRequest request, ClientHttpResponse response) {
    return "12345";
  }
}