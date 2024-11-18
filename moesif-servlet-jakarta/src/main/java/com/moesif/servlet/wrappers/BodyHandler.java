package com.moesif.servlet.wrappers;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class BodyHandler {
  public static boolean logBody = true;

  public static String encodeContent(byte[] content, String encoding) {
    try {
      return new String(content, encoding != null ? encoding : StandardCharsets.UTF_8.name());
    } catch (UnsupportedEncodingException e) {
      return "[UNSUPPORTED ENCODING]";
    }
  }

  // a method that returns a simple java map representing an error message for large body meant to be serialized into json
  public static Map<String, String> getLargeBodyError(long contentLength, long maxBodySize) {
    Map<String, String> error = new HashMap<>();
    error.put("msg", "The body length " + contentLength + " exceeded the maximum allowed size of  " + maxBodySize + " bytes");
    return error;
  }

}
