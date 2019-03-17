package com.moesif.servlet.wrappers;

import org.apache.commons.lang3.StringUtils;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.*;

import static java.nio.charset.StandardCharsets.UTF_8;

public class LoggingHttpServletResponseWrapper extends HttpServletResponseWrapper {

  private final LoggingServletOutputStream loggingServletOutputStream = new LoggingServletOutputStream();

  public final HttpServletResponse delegate;

  public LoggingHttpServletResponseWrapper(HttpServletResponse response) {
    super(response);
    delegate = response;
  }

  @Override
  public ServletOutputStream getOutputStream() throws IOException {
    return loggingServletOutputStream;
  }

  @Override
  public PrintWriter getWriter() throws IOException {
    return new PrintWriter(loggingServletOutputStream.baos, true);
  }

  public Map<String, String> getHeaders() {
    Map<String, String> headers = new HashMap<String, String>(0);
    Collection<String> headerNames = getHeaderNames();

    for (String headerName: headerNames) {
      if (headerName != null) {
        if (headerName.equals("set-cookie")) {
          headers.put(headerName, getHeader(headerName));
        } else {
          headers.put(headerName, StringUtils.join(getHeaders(headerName), ","));
        }
      }
    }
    return headers;
  }

  public String getContent() {
    try {
      String responseEncoding = delegate.getCharacterEncoding();
      return loggingServletOutputStream.baos.toString(responseEncoding != null ? responseEncoding : UTF_8.name());
    } catch (UnsupportedEncodingException e) {
      return "[UNSUPPORTED ENCODING]";
    }
  }

  public byte[] getContentAsBytes() {
    return loggingServletOutputStream.baos.toByteArray();
  }

  private class LoggingServletOutputStream extends ServletOutputStream {

    private ByteArrayOutputStream baos = new ByteArrayOutputStream();

    @Override
    public boolean isReady() {
      return true;
    }

    @Override
    public void setWriteListener(WriteListener writeListener) {
    }

    @Override
    public void write(int b) throws IOException {
      baos.write(b);
    }

    @Override
    public void write(byte[] b) throws IOException {
      baos.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
      baos.write(b, off, len);
    }
  }
}
