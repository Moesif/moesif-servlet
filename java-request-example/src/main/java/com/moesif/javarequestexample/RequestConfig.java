package com.moesif.javarequestexample;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import com.moesif.javarequest.MoesifRequestConfiguration;

public class RequestConfig extends MoesifRequestConfiguration {
  @Override
  public String identifyUser(HttpRequest request, ClientHttpResponse response) {
    return "test_user";
  }
}