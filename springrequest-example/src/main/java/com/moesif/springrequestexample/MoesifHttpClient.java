package com.moesif.springrequestexample;

import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

import com.moesif.springrequest.MoesifSpringRequestInterceptor;

import java.util.ArrayList;
import java.util.List;

public class MoesifHttpClient {
  private RestTemplate template;

  public MoesifHttpClient() {
    template = new RestTemplate();

    final List<ClientHttpRequestInterceptor> interceptors = new ArrayList<ClientHttpRequestInterceptor>();

    interceptors.add(new MoesifSpringRequestInterceptor(
      "Enter your Moesif AppId here",
      new RequestConfig()
    ));

    template.setInterceptors( interceptors );
  }

  public RestTemplate getRestTemplate() {
    return template;
  }
}