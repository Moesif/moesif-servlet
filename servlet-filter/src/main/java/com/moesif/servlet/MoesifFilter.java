package com.moesif.servlet;

import java.io.IOException;
import java.util.logging.Logger;
import java.util.Date;
import java.util.HashMap;
import com.moesif.servlet.utils.Base64;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.moesif.api.models.EventModel;
import com.moesif.api.models.EventRequestModel;
import com.moesif.api.models.EventResponseModel;
import com.moesif.api.models.EventBuilder;
import com.moesif.api.models.EventRequestBuilder;
import com.moesif.api.models.EventResponseBuilder;

import com.moesif.api.APIHelper;
import com.moesif.api.MoesifAPIClient;
import com.moesif.api.http.client.APICallBack;
import com.moesif.api.http.client.HttpContext;

import com.moesif.servlet.wrappers.LoggingHttpServletRequestWrapper;
import com.moesif.servlet.wrappers.LoggingHttpServletResponseWrapper;

public class MoesifFilter implements Filter {

  private static final Logger logger = Logger.getLogger(MoesifFilter.class.toString());

  private String applicationId;
  private MoesifConfiguration config;
  private MoesifAPIClient moesifApi;
  private boolean debug;

  /**
   * Default Constructor, please set ApplicationId before use.
   */
  public MoesifFilter() {
    this.config = new MoesifConfigurationAdapter();
    this.debug = false;
  }

  /**
   * Constructor
   * @param    applicationId   Required parameter: obtained from your moesif Account.
   */
  public MoesifFilter(String applicationId) {
    this.applicationId = applicationId;
    this.config = new MoesifConfigurationAdapter();
    this.moesifApi = new MoesifAPIClient(applicationId);
    this.debug = false;
  }

  /**
   * Constructor
   * @param    applicationId   Required parameter: obtained from your moesif Account.
   * @param    debug Flag for turning debug messages on.
   */
  public MoesifFilter(String applicationId, boolean debug) {
    this.applicationId = applicationId;
    this.config = new MoesifConfigurationAdapter();
    this.moesifApi = new MoesifAPIClient(applicationId);
    this.debug = debug;
  }

  /**
   * Constructor
   * @param    applicationId   Required parameter: obtained from your moesif Account.
   * @param    config MoesifConfiguration Object.
   */
  public MoesifFilter(String applicationId, MoesifConfiguration config) {
    this.applicationId = applicationId;
    this.config = config;
    this.moesifApi = new MoesifAPIClient(applicationId);
    this.debug = false;
  }

  /**
   * Constructor
   * @param    applicationId   Required parameter: obtained from your moesif Account.
   * @param    config MoesifConfiguration Object
   * @param    debug boolean
   */
  public MoesifFilter(String applicationId, MoesifConfiguration config, boolean debug) {
    this.applicationId = applicationId;
    this.config = config;
    this.moesifApi = new MoesifAPIClient(applicationId);
    this.debug = debug;
  }

  /**
   * Sets the Moesif Application Id.
   * @param    applicationId   Required parameter: obtained from your moesif Account.
   */
  public void setApplicationId(String applicationId) {
    this.applicationId = applicationId;
    this.moesifApi = new MoesifAPIClient(applicationId);
  }

  /**
   * Sets the MoesifConfiguration
   * @param    config MoesifConfiguration Object
   */
  public void setConfigure(MoesifConfiguration config) {
    this.config = config;
  }

  /**
   * Sets the debug flag
   * @param    debug boolean
   */
  public void setDebug(boolean debug) {
    this.debug = debug;
  }

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    if (debug) {
      logger.fine("moesif filter init called");
    }
    String appId = filterConfig.getInitParameter("application-id");
    if (appId != null) {
      this.applicationId = appId;
      this.moesifApi = new MoesifAPIClient(this.applicationId);
    }
    String debug = filterConfig.getInitParameter("debug");
    if (debug != null) {
      if (debug.equals("true")) {
        this.debug = true;
      }
    }
  }

  @Override
  public void destroy() {
    if (debug) {
      logger.fine("moesif filter destroy called");
    }
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
    if (debug) {
      logger.fine("moesif doFilter called");
    }

    long startTime = System.currentTimeMillis();
    Date startDate = new Date();

    if (!(request instanceof HttpServletRequest) || !(response instanceof HttpServletResponse)) {
      throw new ServletException("MoesifFilter just supports HTTP requests");
    }

    HttpServletRequest httpRequest = (HttpServletRequest) request;
    HttpServletResponse httpResponse = (HttpServletResponse) response;

    if (config.skip(httpRequest, httpResponse)) {
      filterChain.doFilter(httpRequest, httpResponse);
      return;
    }

    LoggingHttpServletRequestWrapper requestWrapper = new LoggingHttpServletRequestWrapper(httpRequest);
    LoggingHttpServletResponseWrapper responseWrapper = new LoggingHttpServletResponseWrapper(httpResponse);

    EventRequestModel eventRequestModel = getEventRequestModel(requestWrapper,
        startDate, config.getApiVersion(httpRequest, httpResponse));

    // pass to next step in the chain.

    filterChain.doFilter(requestWrapper, responseWrapper);

    // logger.info(getResponseDescription(responseWrapper));
    Date endDate = new Date();
    EventResponseModel eventResponseModel = getEventResponseModel(responseWrapper, endDate);

    httpResponse.getOutputStream().write(responseWrapper.getContentAsBytes());

    sendEvent(
        eventRequestModel,
        eventResponseModel,
        config.identifyUser(httpRequest, httpResponse),
        config.getSessionToken(httpRequest, httpResponse),
        config.getTags(httpRequest, httpResponse)
    );
  }


  protected EventRequestModel getEventRequestModel(LoggingHttpServletRequestWrapper requestWrapper, Date date, String apiVersion) {
    EventRequestBuilder eventRequestBuilder = new EventRequestBuilder();
    eventRequestBuilder
        .time(date)
        .uri(getFullURL(requestWrapper))
        .headers(requestWrapper.getHeaders())
        .verb(requestWrapper.getMethod());

    String ipAddress = getIpAddress(requestWrapper);

    if (ipAddress != null) {
      eventRequestBuilder.ipAddress(ipAddress);
    }

    if (apiVersion != null) {
      eventRequestBuilder.apiVersion(apiVersion);
    }

    String content = requestWrapper.getContent();

    if (content != null) {
      if(content.equals("[\"[UNSUPPORTED ENCODING]\"]")) {

        String errorMsgJson = "{"
            + "    \"moesif_error\": {"
            + "       \"code\": \"servlet_content_type_error\","
            + "       \"msg\": \"The content type of the body is not supported.\","
            + "       \"src\": \"moesif-servlet\","
            + "       \"args\": \"\""
            + "    }";

        try {
          Object reqBody = APIHelper.deserialize(errorMsgJson);
          eventRequestBuilder.body(reqBody);
        } catch(Exception E) {
          if (debug) {
            logger.fine("the error message parse failed");
          }
        }
      } else if (isJsonHeader(requestWrapper.getHeaders()) || isStartJson(content)) {
        eventRequestBuilder.body(safeParseJson(content));
      } else {
        eventRequestBuilder.transferEncoding("base64");
        eventRequestBuilder.body(getBase64String(content));
      }
    }

    return eventRequestBuilder.build();
  }

  protected EventResponseModel getEventResponseModel(LoggingHttpServletResponseWrapper responseWrapper, Date date) {
    EventResponseBuilder eventResponseBuilder = new EventResponseBuilder();
    eventResponseBuilder
        .time(date)
        .status(responseWrapper.getStatus())
        .headers(responseWrapper.getHeaders());

    String content = responseWrapper.getContent();

    if (content != null) {
      if(content.equals("[\"[UNSUPPORTED ENCODING]\"]")) {

        String errorMsgJson = "{"
            + "    \"moesif_error\": {"
            + "       \"code\": \"servlet_content_type_error\","
            + "       \"msg\": \"The content type of the body is not supported.\","
            + "       \"src\": \"moesif-servlet\","
            + "       \"args\": \"\""
            + "    }";

        try {
          Object resBody = APIHelper.deserialize(errorMsgJson);
          eventResponseBuilder.body(resBody);
        } catch(Exception E) {
          if (debug) {
            logger.fine("the error message parse failed");
          }
        }
      } else if (isJsonHeader(responseWrapper.getHeaders()) || isStartJson(content)) {
        eventResponseBuilder.body(safeParseJson(content));
      } else {
        eventResponseBuilder.transferEncoding("base64");
        eventResponseBuilder.body(getBase64String(content));
      }
    }

    return eventResponseBuilder.build();
  }

  protected void sendEvent(EventRequestModel eventRequestModel,
                           EventResponseModel eventResponseModel,
                           String userId,
                           String sessionToken,
                           String tags) {
    EventBuilder eb = new EventBuilder();
    eb.request(eventRequestModel);
    eb.response(eventResponseModel);
    if (userId != null) {
      eb.userId(userId);
    }
    if (sessionToken != null) {
      eb.sessionToken(sessionToken);
    }
    if (tags != null) {
      eb.tags(tags);
    }

    EventModel event = eb.build();

    if (this.moesifApi != null) {
      // actually send the event here.

      APICallBack<Object> callBack = new APICallBack<Object>() {
        public void onSuccess(HttpContext context, Object response) {
          if (debug) {
            logger.info("send to Moesif success");
          }
        }

        public void onFailure(HttpContext context, Throwable error) {
          if (debug) {
            logger.info("send to Moesif error ");
            logger.info( error.toString());
          }
        }
      };

      try {

        EventModel maskedEvent = config.maskContent(event);
        if (maskedEvent == null) {
          logger.severe("maskContent() returned a null object, not allowed");
        }

        moesifApi.getAPI().createEventAsync(maskedEvent, callBack);

      } catch(Exception e) {
        if (debug) {
          logger.info("send to Moesif failed");
          logger.info(e.toString());
        }
      }

    } else {
      if (debug) {
        logger.fine("The application Id should be set before using MoesifFilter");
      }
    }
  }

  static boolean isJsonHeader(Map<String, String> headers) {
    // TODO check capitalized case also.
    String val = headers.get("Content-Type");
    if (val != null) {
      if (val.contains("json")) {
        return true;
      }
    }
    String val2 = headers.get("content-type");
    if (val2 != null) {
      if (val2.contains("json")) {
        return true;
      }
    }
    String val3 = headers.get("CONTENT-TYPE");
    if (val3 != null) {
      if (val3.contains("json")) {
        return true;
      }
    }
    return false;
  }

  static boolean isStartJson(String str) {
    return str.trim().startsWith("[") || str.trim().startsWith("{");
  }

  static Object safeParseJson(String str) {
    try {
      return APIHelper.deserialize(str);
    } catch(IOException e) {
      HashMap<String, Object> hmap = new HashMap<String, Object>();
      HashMap<String, String> errorHash = new HashMap<String, String>();
      errorHash.put("code", "json_parse_error");
      errorHash.put("src", "moesif-servlet");
      errorHash.put("msg", "Body is not a valid JSON Objec tor JSON Array");
      errorHash.put("args", str);
      hmap.put("moesif_error", errorHash);
      return hmap;
    }
  }

  static String getBase64String(String str) {
    byte[] encodedBytes = Base64.encode(str.getBytes(), Base64.DEFAULT);
    return new String(encodedBytes);
  }

  static String getIpAddress(HttpServletRequest request) {
    String ipAddress = request.getHeader("X-FORWARDED-FOR");

    if (ipAddress == null) {
      ipAddress = request.getHeader("X-Forwarded-For");
    }

    if (ipAddress == null) {
      ipAddress = request.getHeader("x-forwarded-for");
    }

    if (ipAddress == null) {
      ipAddress = request.getRemoteAddr();
    }
    return ipAddress;
  }

  static String getFullURL(HttpServletRequest request) {
    StringBuffer requestURL = request.getRequestURL();
    String queryString = request.getQueryString();

    if (queryString == null) {
      return requestURL.toString();
    } else {
      return requestURL.append('?').append(queryString).toString();
    }
  }
}