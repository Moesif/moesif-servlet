package com.moesif.servlet.wrappers;

import com.moesif.servlet.MoesifConfiguration;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class LoggingHttpServletResponseWrapper extends HttpServletResponseWrapper {
  public long contentLength = 0;
  public boolean bodySkipped = false;
  private ServletOutputStream outputStream;
  private LoggingServletOutputStream logStream;
  private PrintWriter writer;
  private MoesifConfiguration config;

  public LoggingHttpServletResponseWrapper(HttpServletResponse response, MoesifConfiguration config) {
    super(response);
    this.config = config;
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
      if (logStream.bufferExceeded) {
        bodySkipped = true;
      }
      logStream.flush();
    }
  }

  public Map<String, String> getHeaders() {
    Map<String, String> headers = new HashMap<String, String>(0);
    Collection<String> headerNames = getHeaderNames();

    for (String headerName : headerNames) {
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

  private void readContentLength() {
    try {
      this.contentLength = Long.parseLong(getHeader("Content-Length"));
    } catch (NumberFormatException e) {
      // ignore malformed content length
    }
  }

  private boolean shouldSkipBody() {
    readContentLength();
    // should skip if we are not logging body by config or content length is greater than max body size
    return !BodyHandler.logBody || contentLength > config.maxBodySize;
  }

  public String getContent() {
    try {
      flushBuffer();
      if (shouldSkipBody() || logStream == null || logStream.getBufferedStream() == null) {
        return null;
      }
      updateContentLength(logStream.getBufferedStream().size());
      String responseEncoding = getResponse().getCharacterEncoding();
      return BodyHandler.encodeContent(logStream.getBufferedStream().toByteArray(), responseEncoding);
    } catch (IOException e) {
      return "[IO EXCEPTION]";
    }
  }

  private void updateContentLength(long length) {
    if (contentLength == 0) {
      contentLength = length;
    }
  }

  public class LoggingServletOutputStream extends ServletOutputStream {
    public boolean bufferExceeded = false;
    private ServletOutputStream outputStream;
    private ByteArrayOutputStream baos;

    public LoggingServletOutputStream(ServletOutputStream outputStream) {
      this.outputStream = outputStream;
      this.baos = new ByteArrayOutputStream();
    }

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
      if (!bufferExceeded) {
        if (baos.size() < config.maxBodySize) {
          baos.write(b);
        } else {
          bufferExceeded = true;
          baos.close();
          baos = null;
        }
      }
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
      outputStream.write(b, off, len);
      if (!bufferExceeded) {
        if (baos.size() + len <= config.maxBodySize) {
          baos.write(b, off, len);
        } else {
          bufferExceeded = true;
          baos.close();
          baos = null;
        }
      }
    }

    @Override
    public void flush() throws IOException {
      outputStream.flush();
      if (!bufferExceeded) {
        baos.flush();
      }
    }

    @Override
    public void close() throws IOException {
      outputStream.close();
      if (baos != null) baos.close();
    }

    public ByteArrayOutputStream getBufferedStream() {
      return baos;
    }
  }
}
