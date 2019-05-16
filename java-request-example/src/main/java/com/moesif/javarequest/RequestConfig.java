package com.moesif.javarequest;

import com.moesif.moesifjavarequest.MoesifRequestConfiguration;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpResponse;

public class RequestConfig extends MoesifRequestConfiguration {
  @Override
  public String identifyUser(HttpRequest request, ClientHttpResponse response) {
    return "test_user";
  }
}