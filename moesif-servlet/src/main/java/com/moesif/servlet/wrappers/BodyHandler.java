package com.moesif.servlet.wrappers;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class BodyHandler {

  public static final int MAX_BODY_SIZE = 10; // Move to a common utility class
  public static boolean logBody = true;

  public static String encodeContent(byte[] content, String encoding) {
    try {
      return new String(content, encoding != null ? encoding : StandardCharsets.UTF_8.name());
    } catch (UnsupportedEncodingException e) {
      return "[UNSUPPORTED ENCODING]";
    }
  }

  // a method that returns a simple java map representing an error message for large body meant to be serialized into json
  public static Map<String, String> getLargeBodyError(long contentLength) {
    Map<String, String> error = new HashMap<>();
    error.put("msg", "request.body.length " + contentLength + " exceeded requestMaxBodySize of  " + MAX_BODY_SIZE + " bytes");
    return error;
  }

}
