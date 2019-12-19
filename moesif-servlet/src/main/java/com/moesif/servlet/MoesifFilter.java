package com.moesif.servlet;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;
import java.lang.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.moesif.api.models.*;

import com.moesif.api.MoesifAPIClient;
import com.moesif.api.http.client.APICallBack;
import com.moesif.api.http.client.HttpContext;
import com.moesif.api.http.response.HttpResponse;
import com.moesif.api.controllers.APIController;
import com.moesif.api.IpAddress;
import com.moesif.api.BodyParser;

import com.moesif.servlet.wrappers.LoggingHttpServletRequestWrapper;
import com.moesif.servlet.wrappers.LoggingHttpServletResponseWrapper;
import org.apache.commons.lang3.StringUtils;

public class MoesifFilter implements Filter {

  private static final Logger logger = Logger.getLogger(MoesifFilter.class.toString());

  private String applicationId;
  private MoesifConfiguration config;
  private MoesifAPIClient moesifApi;
  private boolean debug;
  private boolean logBody;
  private AppConfigModel appConfigModel = new AppConfigModel();
  private String cachedConfigEtag;
  private Date lastUpdatedTime = new Date(0);

  /**
   * Default Constructor, please set ApplicationId before use.
   */
  public MoesifFilter() {
    this.config = new MoesifConfigurationAdapter();
    this.debug = false;
    this.logBody = true;
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
    this.logBody = true;
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
    this.logBody = true;
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
    this.logBody = true;
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
    this.logBody = true;
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

  /**
   * Sets the logBody flag
   * @param    logBody boolean
   */
  public void setLogBody(boolean logBody) {
    this.logBody = logBody;
  }

  /**
   * Get the underlying APIController
   * @return	Returns the APIController instance
   */
  public APIController getAPI() {
    if (moesifApi != null) {
      return moesifApi.getAPI();
    }
    return null;
  }

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    logger.info("Initialized Moesif filter");

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

    String logBody = filterConfig.getInitParameter("logBody");
    if (logBody != null) {
      if (logBody.equals("false")) {
        this.logBody = false;
      }
    }

    getAndUpdateAppConfig();
  }

  @Override
  public void destroy() {
    if (debug) {
      logger.info("Destroyed Moesif filter");
    }
  }
  
  // Get Config. called only when configEtagChange is detected
  public void getAndUpdateAppConfig() {
	  try {
        // Calling the api
        HttpResponse configApiResponse = moesifApi.getAPI().getAppConfig();
        // Fetch the response ETag
        String responseConfigEtag = configApiResponse.getHeaders().get("x-moesif-config-etag");

        // Read the response body
        ObjectMapper mapper = new ObjectMapper();
        AppConfigModel newConfig = mapper.readValue(configApiResponse.getRawBody(), AppConfigModel.class);

        this.appConfigModel = newConfig;
        this.cachedConfigEtag = responseConfigEtag;
      } catch(Throwable e) {
        logger.warning("Fetched configuration failed; using default configuration " + e.toString());
        this.appConfigModel = new AppConfigModel();
        this.appConfigModel.setSampleRate(100);
      }
    this.lastUpdatedTime = new Date();
  }

  public void updateUser(UserModel userModel) throws Throwable{

    if (this.moesifApi != null) {
      String userId = userModel.getUserId();
      if (userId != null && !userId.isEmpty()) {
        try {
          moesifApi.getAPI().updateUser(userModel);
        }
        catch(Exception e) {
          if (debug) {
            logger.warning("Update User to Moesif failed " + e.toString());
          }
        }
      }
      else {
        throw new IllegalArgumentException("To update an user, an userId field is required");
      }
    }
    else {
      logger.warning("The application Id should be set before using MoesifFilter");
    }
  }

  public void updateUsersBatch(List<UserModel> usersModel) throws Throwable{

    List<UserModel> users = new ArrayList<UserModel>();
    if (this.moesifApi != null) {
      for (UserModel user : usersModel) {
        String userId = user.getUserId();
        if (userId != null && !userId.isEmpty()) {
          users.add(user);
        } else {
          throw new IllegalArgumentException("To update an user, an userId field is required");
        }
      }
    }
    else {
      logger.warning("The application Id should be set before using MoesifFilter");
    }

    if (!users.isEmpty()) {
      try {
        moesifApi.getAPI().updateUsersBatch(users);
      } catch (Exception e) {
        if (debug) {
          logger.warning("Update User to Moesif failed " + e.toString());
        }
      }
    }
  }

  public void updateCompany(CompanyModel companyModel) throws Throwable{

    if (this.moesifApi != null) {
      String companyId = companyModel.getCompanyId();
      if (companyId != null && !companyId.isEmpty()) {
        try {
          moesifApi.getAPI().updateCompany(companyModel);
        }
        catch(Exception e) {
          if (debug) {
            logger.warning("Update Company to Moesif failed " + e.toString());
          }
        }
      }
      else {
        throw new IllegalArgumentException("To update a company, a companyId field is required");
      }
    }
    else {
      logger.warning("The application Id should be set before using MoesifFilter");
    }
  }

  public void updateCompaniesBatch(List<CompanyModel> companiesModel) throws Throwable{

    List<CompanyModel> companies = new ArrayList<CompanyModel>();
    if (this.moesifApi != null) {
      for (CompanyModel company : companiesModel) {
        String companyId = company.getCompanyId();
        if (companyId != null && !companyId.isEmpty()) {
          companies.add(company);
        } else {
          throw new IllegalArgumentException("To update a company, a companyId field is required");
        }
      }
    }
    else {
      logger.warning("The application Id should be set before using MoesifFilter");
    }

    if (!companies.isEmpty()) {
      try {
        moesifApi.getAPI().updateCompaniesBatch(companies);
      } catch (Exception e) {
        if (debug) {
          logger.warning("Update Companies to Moesif failed " + e.toString());
        }
      }
    }
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
    if (debug) {
      logger.info("filtering request");
    }

    Date startDate = new Date();

    if (!(request instanceof HttpServletRequest) || !(response instanceof HttpServletResponse)) {
      logger.warning("MoesifFilter was called for non HTTP requests");
      filterChain.doFilter(request, response);
      return;
    }

    HttpServletRequest httpRequest = (HttpServletRequest) request;
    HttpServletResponse httpResponse = (HttpServletResponse) response;

    if (config.skip(httpRequest, httpResponse)) {
      filterChain.doFilter(httpRequest, httpResponse);
        if (debug) { logger.warning("skipping request"); }
        return;
    }

    LoggingHttpServletRequestWrapper requestWrapper = new LoggingHttpServletRequestWrapper(httpRequest);
    LoggingHttpServletResponseWrapper responseWrapper = new LoggingHttpServletResponseWrapper(httpResponse);


    // Initialize transactionId    
    String transactionId = null;

    if (!config.disableTransactionId) {
    	
    	String reqTransId = requestWrapper.getHeader("X-Moesif-Transaction-Id"); 
        
        if (reqTransId != null && !reqTransId.isEmpty()) {
        	transactionId = reqTransId;
        } else {
        	transactionId = UUID.randomUUID().toString();
        }

        // Add Transaction Id to the response model and response sent to the client
        responseWrapper.addHeader("X-Moesif-Transaction-Id", transactionId);	
    }

    EventRequestModel eventRequestModel = getEventRequestModel(requestWrapper,
        startDate, config.getApiVersion(httpRequest, httpResponse), transactionId);

    // pass to next step in the chain.
    try {
      filterChain.doFilter(requestWrapper, responseWrapper);
    } finally {
      Date endDate = new Date();
      EventResponseModel eventResponseModel = getEventResponseModel(responseWrapper, endDate);

      if (!(responseWrapper.getResponse() instanceof LoggingHttpServletResponseWrapper)) {
        sendEvent(
              eventRequestModel,
              eventResponseModel,
              config.identifyUser(httpRequest, httpResponse),
              config.identifyCompany(httpRequest, httpResponse),
              config.getSessionToken(httpRequest, httpResponse),
              config.getTags(httpRequest, httpResponse),
              config.getMetadata(httpRequest, httpResponse)
        );
      }
    }
  }


  private EventRequestModel getEventRequestModel(LoggingHttpServletRequestWrapper requestWrapper, Date date, String apiVersion, String transactionId) {
    EventRequestBuilder eventRequestBuilder = new EventRequestBuilder();
    // Add Transaction Id to the request model
    Map<String, String> reqHeaders = new HashMap<String, String>(0);
    if (transactionId != null) {
    	reqHeaders = requestWrapper.addHeader("X-Moesif-Transaction-Id", transactionId);
    } else {
    	reqHeaders = requestWrapper.getHeaders();
    }
    eventRequestBuilder
        .time(date)
        .uri(getFullURL(requestWrapper))
        .headers(reqHeaders)
        .verb(requestWrapper.getMethod())
        .ipAddress(IpAddress.getClientIp(
          requestWrapper.getHeaders(),
          requestWrapper.getRemoteAddr()
        ));

    if (StringUtils.isNotEmpty(apiVersion)) {
      eventRequestBuilder.apiVersion(apiVersion);
    }

    String content = requestWrapper.getContent();

    if (logBody && content != null  && !content.isEmpty()) {
      BodyParser.BodyWrapper bodyWrapper = BodyParser.parseBody(requestWrapper.getHeaders(), content);
      eventRequestBuilder.body(bodyWrapper.body);
      eventRequestBuilder.transferEncoding(bodyWrapper.transferEncoding);
    }

    return eventRequestBuilder.build();
  }

  private EventResponseModel getEventResponseModel(LoggingHttpServletResponseWrapper responseWrapper, Date date) {
    EventResponseBuilder eventResponseBuilder = new EventResponseBuilder();
    eventResponseBuilder
        .time(date)
        .status(responseWrapper.getStatus())
        .headers(responseWrapper.getHeaders());

    String content = responseWrapper.getContent();

    if (logBody && content != null  && !content.isEmpty()) {
      BodyParser.BodyWrapper bodyWrapper = BodyParser.parseBody(responseWrapper.getHeaders(), content);
      eventResponseBuilder.body(bodyWrapper.body);
      eventResponseBuilder.transferEncoding(bodyWrapper.transferEncoding);
    }

    return eventResponseBuilder.build();
  }

  private void sendEvent(EventRequestModel eventRequestModel,
                           EventResponseModel eventResponseModel,
                           String userId,
                           String companyId,
                           String sessionToken,
                           String tags,
                           Object metadata) {
    EventBuilder eb = new EventBuilder();
    eb.request(eventRequestModel);
    eb.response(eventResponseModel);
    eb.direction("Incoming");
    if (userId != null) {
      eb.userId(userId);
    }
    if (companyId != null) {
      eb.companyId(companyId);
    }
    if (sessionToken != null) {
      eb.sessionToken(sessionToken);
    }
    if (tags != null) {
      eb.tags(tags);
    }
    if (metadata != null) {
      eb.metadata(metadata);
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

        // Generate random number
        double randomPercentage = Math.random() * 100;

        int samplingPercentage = getSampleRateToUse(userId, companyId);

        // Compare percentage to send event
        if (samplingPercentage >= randomPercentage) {
            maskedEvent.setWeight(moesifApi.getAPI().calculateWeight(samplingPercentage));
        	// Send Event
        	Map<String, String> eventApiResponse = moesifApi.getAPI().createEvent(maskedEvent);
        	// Get the etag from event api response
        	String eventResponseConfigEtag = eventApiResponse.get("x-moesif-config-etag");
        	
        	// Check if needed to call the getConfig api to update samplingPercentage
        	if (eventResponseConfigEtag != null 
        			&& !(eventResponseConfigEtag.equals(cachedConfigEtag)) 
        			&& new Date().after(new Date(this.lastUpdatedTime.getTime() + 5 * 60 * 1000))) {
        		// Call api to update samplingPercentage
        		getAndUpdateAppConfig();
        	}
        	
        	if (debug) {
                logger.info("Event successfully sent to Moesif");
              }
        } 
        else {
        	if(debug) {
        		logger.info("Skipped Event due to SamplingPercentage " + samplingPercentage + " and randomPercentage " + randomPercentage);
        	}
        }

      } catch(Throwable e) {
        if (debug) {
          logger.warning("send to Moesif failed " + e.toString());
        }
      }

    } else {
      logger.warning("The application Id should be set before using MoesifFilter");
    }
  }

  public int getSampleRateToUse(String userId, String companyId) {
    int sampleRate = appConfigModel.getSampleRate();
    if (userId != null && appConfigModel.getUserSampleRate().containsKey(userId)) {
      sampleRate = appConfigModel.getUserSampleRate().get(userId);
    } else if (companyId != null && appConfigModel.getCompanySampleRate().containsKey(companyId)) {
      sampleRate = appConfigModel.getCompanySampleRate().get(companyId);
    }
    return sampleRate;
  }

  static String getFullURL(HttpServletRequest request) {
    StringBuffer requestURL = request.getRequestURL();
    String queryString = request.getQueryString();

    if (requestURL == null) {
      return "/";
    } else if (queryString == null) {
      return requestURL.toString();
    } else {
      return requestURL.append('?').append(queryString).toString();
    }
  }
}