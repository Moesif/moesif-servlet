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

    RequestConfig requestConfig = new RequestConfig();
    
    // Set logBody flag to false to remove logging request and response body to Moesif
    requestConfig.logBody = true;

    interceptors.add(new MoesifSpringRequestInterceptor(
      "Enter your Moesif AppId here",
      requestConfig
    ));

    template.setInterceptors( interceptors );
  }

  public RestTemplate getRestTemplate() {
    return template;
  }
}