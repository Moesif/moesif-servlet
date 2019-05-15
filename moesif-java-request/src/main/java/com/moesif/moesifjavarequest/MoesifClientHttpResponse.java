package com.moesif.moesifjavarequest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;

/**
 * The body of ClientHttpResponse is an InputStream which can only be read once.
 * 
 * Since we want to report the body contents without disrupting the application
 * code which depends on the response body, we must create a wrapper object.
 */
public class MoesifClientHttpResponse implements ClientHttpResponse {
  private ClientHttpResponse response;
  private String bodyString;
  private InputStream inputStream;

  MoesifClientHttpResponse(ClientHttpResponse response) throws IOException {
    this.response = response;

    InputStream bodyStream = response.getBody();
    byte[] bodyBytes = bodyStream.readAllBytes();

    bodyString = new String(bodyBytes);
    inputStream = new ByteArrayInputStream(bodyBytes);
  }

  public String getBodyString() {
    return bodyString;
  }

  @Override
  public InputStream getBody() throws IOException {
    // publicly reuse the same InputStream to keep the same behavior
    // (can only read once)
    return inputStream;
  }

  // wrap all methods required by ClientHttpResponse
  @Override
  public int getRawStatusCode() throws IOException {
    return response.getRawStatusCode();
  }

  @Override
  public HttpStatus getStatusCode() throws IOException {
    return response.getStatusCode();
  }

  @Override
  public void close() {
    response.close();
  }

  @Override
  public HttpHeaders getHeaders() {
    return response.getHeaders();
  }

  @Override
  public String getStatusText() throws IOException {
    return response.getStatusText();
  }
}