package com.moesif.springrequest;

import com.moesif.api.models.EventResponseModel;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.MultiValueMap;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;


public class MoesifBlockedClientHttpResponse implements ClientHttpResponse {
  private EventResponseModel response;



  MoesifBlockedClientHttpResponse(EventResponseModel response)  {
    this.response = response;
  }


  @Override
  public InputStream getBody() throws IOException {
    return new ByteArrayInputStream(response.getBody().toString().getBytes());
  }

  // wrap all methods required by ClientHttpResponse
  @Override
  public int getRawStatusCode() throws IOException {
    return response.getStatus();
  }

  @Override
  public HttpStatus getStatusCode() throws IOException {
    return HttpStatus.valueOf(getRawStatusCode());
  }

  @Override
  public void close() {

  }

  @Override
  public HttpHeaders getHeaders() {
    HttpHeaders headers = new HttpHeaders();
    for (String key : response.getHeaders().keySet()) {
      headers.put(key, Arrays.asList(response.getHeaders().get(key)));
    }
    return headers;
  }

  @Override
  public String getStatusText() throws IOException {
    return getStatusCode().getReasonPhrase();
  }
}