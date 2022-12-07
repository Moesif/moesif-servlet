package com.moesif.servlet.wrappers;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;


import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class LoggingHttpServletRequestWrapper extends HttpServletRequestWrapper {

  private static final List<String> FORM_CONTENT_TYPE = Arrays.asList("application/x-www-form-urlencoded", "multipart/form-data");

  private static final String METHOD_POST = "POST";

  private byte[] content;

  private final Map<String, String[]> parameterMap;

  private final HttpServletRequest delegate;

  public LoggingHttpServletRequestWrapper(HttpServletRequest request) {
    super(request);
    this.delegate = request;
    if (isFormPost()) {
      this.parameterMap = request.getParameterMap();
    } else {
      this.parameterMap = Collections.emptyMap();
    }
  }

  @Override
  public ServletInputStream getInputStream() throws IOException {
    if (ArrayUtils.isEmpty(content)) {
      return delegate.getInputStream();
    }
    return new LoggingServletInputStream(content);
  }

  @Override
  public BufferedReader getReader() throws IOException {
    if (ArrayUtils.isEmpty(content)) {
      return delegate.getReader();
    }
    return new BufferedReader(new InputStreamReader(getInputStream()));
  }

  @Override
  public String getParameter(String name) {
    if (ArrayUtils.isEmpty(content) || this.parameterMap.isEmpty()) {
      return super.getParameter(name);
    }
    String[] values = this.parameterMap.get(name);
    if (values != null && values.length > 0) {
      return values[0];
    }
    return Arrays.toString(values);
  }

  @Override
  public Map<String, String[]> getParameterMap() {
    if (ArrayUtils.isEmpty(content) || this.parameterMap.isEmpty()) {
      return super.getParameterMap();
    }
    return this.parameterMap;
  }

  @Override
  public Enumeration<String> getParameterNames() {
    if (ArrayUtils.isEmpty(content) || this.parameterMap.isEmpty()) {
      return super.getParameterNames();
    }
    return new ParamNameEnumeration(this.parameterMap.keySet());
  }

  @Override
  public String[] getParameterValues(String name) {
    if (ArrayUtils.isEmpty(content) || this.parameterMap.isEmpty()) {
      return super.getParameterValues(name);
    }
    return this.parameterMap.get(name);
  }

  public String getContent() {
    try {
      if (this.parameterMap.isEmpty()) {
        content = IOUtils.toByteArray(delegate.getInputStream());
      } else {
        content = getContentFromParameterMap(this.parameterMap);
      }
      String requestEncoding = delegate.getCharacterEncoding();
      String normalizedContent = StringUtils.normalizeSpace(new String(content, requestEncoding != null ? requestEncoding : StandardCharsets.UTF_8.name()));
      return normalizedContent;
      // return StringUtils.isBlank(normalizedContent) ? "[EMPTY]" : normalizedContent;
    } catch (IOException e) {
      e.printStackTrace();
      throw new IllegalStateException();
    }
  }

  private byte[] getContentFromParameterMap(Map<String, String[]> parameterMap) {

    List<String> result = new ArrayList<String>();
    for (Map.Entry<String, String[]>  e: parameterMap.entrySet()) {
      String[] value = e.getValue();
      result.add(e.getKey() + "=" + (value.length == 1 ? value[0] : Arrays.toString(value)));
    }
    return StringUtils.join(result, "&").getBytes();
  }
  
  // Wrapper function to addHeader
  public Map<String, String> addHeader(String headerKey, String headerValue) {
	  Map<String, String> headers = new HashMap<String, String>(0);
	  headers = getHeaders();
	  headers.put(headerKey, headerValue);
	  // Remove header as the case is not preserved
	  headers.remove("x-moesif-transaction-id");
	  return headers;
  }

  public Map<String, String> getHeaders() {
    Map<String, String> headers = new HashMap<String, String>(0);
    Enumeration<String> headerNames = getHeaderNames();
    while (headerNames.hasMoreElements()) {
      String headerName = headerNames.nextElement();

      if (headerName != null) {
        headers.put(headerName, StringUtils.join(Collections.list(getHeaders(headerName)), ","));
      }
    }
    return headers;
  }

  public boolean isFormPost() {
    String contentType = getContentType();
    if (contentType != null && METHOD_POST.equalsIgnoreCase(getMethod())) {
      for (String formType: FORM_CONTENT_TYPE) {
        if (contentType.toLowerCase().contains(formType)) {
          return true;
        }
      }
    }
    return false;
  }

  private class ParamNameEnumeration implements Enumeration<String> {

    private final Iterator<String> iterator;

    private ParamNameEnumeration(Set<String> values) {
      Iterator<String> emptyIterator = Collections.emptyIterator();
      this.iterator = values != null ? values.iterator() : emptyIterator;
    }

    @Override
    public boolean hasMoreElements() {
      return iterator.hasNext();
    }

    @Override
    public String nextElement() {
      return iterator.next();
    }
  }

  private class LoggingServletInputStream extends ServletInputStream {

    private final InputStream is;

    private LoggingServletInputStream(byte[] content) {
      this.is = new ByteArrayInputStream(content);
    }

    @Override
    public boolean isFinished() {
      return true;
    }

    @Override
    public boolean isReady() {
      return true;
    }

    @Override
    public void setReadListener(ReadListener readListener) {
    }

    @Override
    public int read() throws IOException {
      return this.is.read();
    }

    @Override
    public void close() throws IOException {
      super.close();
      is.close();
    }
  }
}
