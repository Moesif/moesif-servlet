package com.moesif.servlet.wrappers;

import org.apache.commons.lang3.StringUtils;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.*;
import java.util.*;

import static java.nio.charset.StandardCharsets.UTF_8;

public class LoggingHttpServletResponseWrapper extends HttpServletResponseWrapper {
  private ServletOutputStream outputStream;
  private LoggingServletOutputStream logStream;
  private PrintWriter writer;

  public LoggingHttpServletResponseWrapper(HttpServletResponse response) {
    super(response);
  }

  @Override
  public ServletOutputStream getOutputStream() throws IOException {
    if (writer != null) {
      throw new IllegalStateException("getWriter() has already been called on this response.");
    }

    if (outputStream == null) {
      outputStream = getResponse().getOutputStream();
      logStream = new LoggingServletOutputStream(outputStream);
    }

    return logStream;
  }

  @Override
  public PrintWriter getWriter() throws IOException {
    if (outputStream != null) {
      throw new IllegalStateException("getOutputStream() has already been called on this response.");
    }

    if (writer == null) {
      logStream = new LoggingServletOutputStream(getResponse().getOutputStream());
      writer = new PrintWriter(new OutputStreamWriter(logStream, getResponse().getCharacterEncoding()), true);
    }

    return writer;
  }

  @Override
  public void flushBuffer() throws IOException {
    if (writer != null) {
      writer.flush();
    } else if (outputStream != null) {
      logStream.flush();
    }
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
      flushBuffer();
      if (logStream == null) {
        return null;
      }
      String responseEncoding = getResponse().getCharacterEncoding();
      return logStream.baos.toString(responseEncoding != null ? responseEncoding : UTF_8.name());
    } catch (UnsupportedEncodingException e) {
      return "[UNSUPPORTED ENCODING]";
    } catch (IOException e) {
      return "[IO EXCEPTION]";
    }
  }

  private class LoggingServletOutputStream extends ServletOutputStream {
    public LoggingServletOutputStream(ServletOutputStream outputStream) {

      this.outputStream = outputStream;
      this.baos = new ByteArrayOutputStream(1024);
    }

    private ServletOutputStream outputStream;
    private ByteArrayOutputStream baos;

    @Override
    public boolean isReady() {
      return outputStream.isReady();
    }

    @Override
    public void setWriteListener(WriteListener writeListener) {
      outputStream.setWriteListener(writeListener);
    }

    @Override
    public void write(int b) throws IOException {
      outputStream.write(b);
      baos.write(b);
    }

    @Override
    public void write(byte[] b) throws IOException {
      outputStream.write(b);
      baos.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
      outputStream.write(b, off, len);
      baos.write(b, off, len);
    }

    @Override
    public void flush() throws IOException {
      outputStream.flush();
      baos.flush();
    }

    public void close() throws IOException {
      outputStream.close();
      baos.close();
    }
  }
}
