package com.moesif.servlet;

import com.mashape.unirest.request.body.Body;
import com.moesif.api.BodyParser;
import com.moesif.api.IpAddress;
import com.moesif.api.MoesifAPIClient;
import com.moesif.api.controllers.APIController;
import com.moesif.api.models.*;
import com.moesif.servlet.MoesifServletHelper;
import com.moesif.servlet.wrappers.BodyHandler;
import com.moesif.servlet.wrappers.LoggingHttpServletRequestWrapper;
import com.moesif.servlet.wrappers.LoggingHttpServletResponseWrapper;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class MoesifFilter implements Filter {

  private static final Logger logger = Logger.getLogger(MoesifFilter.class.toString());

  private String applicationId;
  private MoesifConfiguration config;
  private MoesifAPIClient moesifApi;
  private boolean debug;
  private boolean logBody;
  private BatchProcessor batchProcessor = null; // Manages queue & provides a taskRunner to send events in batches.
  private int sendBatchJobAliveCounter = 0;     // counter to check scheduled job is alive or not.

  // Timer for various tasks
  Timer updateConfigTimer = null;
  Timer sendBatchEventTimer = null;

  /**
   * Default Constructor, please set ApplicationId before use.
   */
  public MoesifFilter() {
    this.setDebug(false);
    this.setLogBody(true);
    this.setConfigure( new MoesifConfigurationAdapter() );
  }

  /**
   * Constructor
   * @param    applicationId   Required parameter: obtained from your moesif Account.
   */
  public MoesifFilter(String applicationId) {
    this.setDebug(false);
    this.setLogBody(true);
    this.setConfigure( new MoesifConfigurationAdapter() );
    this.setApplicationId(applicationId);
  }

  /**
   * Constructor
   * @param    applicationId   Required parameter: obtained from your moesif Account.
   * @param    debug Flag for turning debug messages on.
   */
  public MoesifFilter(String applicationId, boolean debug) {
    this.setDebug(debug);
    this.setLogBody(true);
    this.setConfigure( new MoesifConfigurationAdapter() );
    this.setApplicationId(applicationId);
  }

  /**
   * Constructor
   * @param    applicationId   Required parameter: obtained from your moesif Account.
   * @param    config MoesifConfiguration Object.
   */
  public MoesifFilter(String applicationId, MoesifConfiguration config) {
    this.setDebug(false);
    this.setLogBody(true);
    this.setConfigure(config);
    this.setApplicationId(applicationId);
  }

  /**
   * Constructor
   * @param    applicationId   Required parameter: obtained from your moesif Account.
   * @param    config MoesifConfiguration Object
   * @param    debug boolean
   */
  public MoesifFilter(String applicationId, MoesifConfiguration config, boolean debug) {
    this.setDebug(debug);
    this.setLogBody(true);
    this.setConfigure(config);
    this.setApplicationId(applicationId);
  }

  /**
   * Constructor for testing purposes.
   * @param    moesifApi   mocked moesifApi client.
   */
  MoesifFilter(MoesifAPIClient moesifApi) {
    this.setDebug(false);
    this.setLogBody(true);
    this.setConfigure( new MoesifConfigurationAdapter() );
    this.moesifApi = moesifApi;
  }

  /**
   * Sets the Moesif Application Id.
   * @param    applicationId   Required parameter: obtained from your moesif Account.
   */
  public void setApplicationId(String applicationId) {
    this.applicationId = applicationId;
    this.createMoesifApiClient();
  }

  /***
   * Creates Moesif API client for given application id.
   */
  private void createMoesifApiClient() {
    this.moesifApi = new MoesifAPIClient(this.applicationId);
    this.moesifApi.setUserAgent(buildUserAgent());
  }

  /**
   * Sets the MoesifConfiguration
   * @param    config MoesifConfiguration Object
   */
  public void setConfigure(MoesifConfiguration config) {
    this.config = config;
    
    // Apply user agent configuration if API client exists
    if (this.moesifApi != null) {
      this.moesifApi.setUserAgent(buildUserAgent());
    }
  }

  /**
   * Builds the User-Agent string.
   * @return The User-Agent string.
   */
  private String buildUserAgent() {
    String servletVersion = MoesifServletHelper.getVersion();
    String baseUserAgent = "moesif-servlet/" + servletVersion;

    if (this.config != null && this.config.userAgent != null && !this.config.userAgent.trim().isEmpty()) {
      return baseUserAgent + " " + this.config.userAgent.trim();
    }
    return baseUserAgent;
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
    BodyHandler.logBody = logBody;
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
      this.setApplicationId(appId);
    }
    String debug = filterConfig.getInitParameter("debug");
    if (debug != null) {
      if (debug.equals("true")) {
        this.setDebug(true);
      }
    }

    String logBody = filterConfig.getInitParameter("logBody");
    if (logBody != null) {
      if (logBody.equals("false")) {
        this.setLogBody(false);
      }
    }

    // Initialize the batch event processor and timer tasks.
    this.initBatchProcessorAndStartJobs();
  }

  @Override
  public void destroy() {

    // Drain the queue and stop the timer tasks.
    this.drainQueueAndStopJobs();

    if (debug) {
      logger.info("Destroyed Moesif filter");
    }
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

  public void updateSubscription(SubscriptionModel subscriptionModel) throws Throwable{

    if (this.moesifApi != null) {
      String subscriptionId = subscriptionModel.getSubscriptionId();
      String companyId = subscriptionModel.getCompanyId();
      String status = subscriptionModel.getStatus();

      if (subscriptionId != null && !subscriptionId.isEmpty() 
          || companyId != null && !companyId.isEmpty()
          || status != null && !status.isEmpty()
          ) {
        try {
          moesifApi.getAPI().updateSubscription(subscriptionModel);
        }
        catch(Exception e) {
          if (debug) {
            logger.warning("Update Subscription to Moesif failed " + e.toString());
          }
        }
      }
      else {
        throw new IllegalArgumentException("To update a subscription, the subscriptionId, companyId, and status fields are required");
      }
    }
    else {
      logger.warning("The application Id should be set before using MoesifFilter");
    }
  }

  public void updateSubscriptionsBatch(List<SubscriptionModel> subscriptionsModel) throws Throwable{

    List<SubscriptionModel> subscriptions = new ArrayList<SubscriptionModel>();
    if (this.moesifApi != null) {
      for (SubscriptionModel subscription : subscriptionsModel) {
        String subscriptionId = subscription.getSubscriptionId();
        String companyId = subscription.getCompanyId();
        String status = subscription.getStatus();

        if (subscriptionId != null && !subscriptionId.isEmpty() 
            || companyId != null && !companyId.isEmpty()
            || status != null && !status.isEmpty()
            ) {
          subscriptions.add(subscription);
        } else {
          throw new IllegalArgumentException("To update a subscription, the subscriptionId, companyId, and status fields are required");
        }
      }
    }
    else {
      logger.warning("The application Id should be set before using MoesifFilter");
    }

    if (!subscriptions.isEmpty()) {
      try {
        moesifApi.getAPI().updateSubscriptionsBatch(subscriptions);
      } catch (Exception e) {
        if (debug) {
          logger.warning("Update Subscriptions to Moesif failed " + e.toString());
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

    LoggingHttpServletRequestWrapper requestWrapper = new LoggingHttpServletRequestWrapper(httpRequest, config);
    LoggingHttpServletResponseWrapper responseWrapper = new LoggingHttpServletResponseWrapper(httpResponse, config);


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

    EventModel event = createEvent(eventRequestModel,
            config.identifyUser(httpRequest, null),
            config.identifyCompany(httpRequest, null),
            config.getSessionToken(httpRequest, null),
            config.getTags(httpRequest, null),
            config.getMetadata(httpRequest, null)
    );

    BlockedByGovernanceRulesResponse blockResponse = moesifApi.getAPI().getBlockedByGovernanceRulesResponse(event);
    if(blockResponse.isBlocked) {
      logger.warning("Blocked by governance rules" + blockResponse.blockedBy);
      event.setBlockedBy(blockResponse.blockedBy);
      event.setResponse(blockResponse.response);
      EventResponseModel responseModel = event.getResponse();
      if (transactionId != null) {
        responseModel.getHeaders().put("X-Moesif-Transaction-Id", transactionId);
      }
      EventModel maskedEvent = config.maskContent(event);
      this.addEventToQueue(maskedEvent);
      Map<String, String> headers = responseModel.getHeaders();
      headers.forEach((key, value) -> {
        httpResponse.setHeader(key, value);
      });
      httpResponse.setStatus(responseModel.getStatus());
      httpResponse.setContentType(responseModel.getTransferEncoding());
      httpResponse.getWriter().write(responseModel.getBody().toString());
      return;
    }


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


    if (this.logBody) {
      String content = requestWrapper.getContent();
      if (content != null && !content.isEmpty()) {
        BodyParser.BodyWrapper bodyWrapper = BodyParser.parseBody(requestWrapper.getHeaders(), content);
        eventRequestBuilder.body(bodyWrapper.body);
        eventRequestBuilder.transferEncoding(bodyWrapper.transferEncoding);
      }
      if (requestWrapper.bodySkipped) {
        eventRequestBuilder.body(BodyHandler.getLargeBodyError(requestWrapper.contentLength, config.requestMaxBodySize));
      }
    }

    return eventRequestBuilder.build();
  }

  private EventResponseModel getEventResponseModel(LoggingHttpServletResponseWrapper responseWrapper, Date date) {
    EventResponseBuilder eventResponseBuilder = new EventResponseBuilder();
    eventResponseBuilder
        .time(date)
        .status(responseWrapper.getStatus())
        .headers(responseWrapper.getHeaders());


    if (this.logBody) {
      String content = responseWrapper.getContent();
      if (content != null && !content.isEmpty()) {
        BodyParser.BodyWrapper bodyWrapper = BodyParser.parseBody(responseWrapper.getHeaders(), content);
        eventResponseBuilder.body(bodyWrapper.body);
        eventResponseBuilder.transferEncoding(bodyWrapper.transferEncoding);
      }
      if (responseWrapper.bodySkipped) {
        eventResponseBuilder.body(BodyHandler.getLargeBodyError(responseWrapper.contentLength, config.requestMaxBodySize));
      }
    }

    return eventResponseBuilder.build();
  }

  /***
   * Method to initialize the batch event processor and create jobs for automatic update of
   * app config and send events in batches.
   * Batch event processor maintains batch queue and a taskRunner method to
   * run scheduled task periodically to send events in batches.
   */
  private void initBatchProcessorAndStartJobs() {

    // Create event batch processor for queueing and batching the events.
    this.batchProcessor = new BatchProcessor(this.moesifApi, this.config, this.debug);

    // Initialize the timer tasks - Create scheduled jobs
//    this.scheduleAppConfigJob();
    this.scheduleBatchEventsJob();
  }

  /***
   * Method to stop the scheduled jobs and drain the batch queue.
   * Batch event processor sends the leftover events present in queue
   * before stopping the scheduled jobs.
   */
  public void drainQueueAndStopJobs() {

    // Cleanup/transfer the leftover queue events, if any before destroying the filter.
    this.batchProcessor.run();  // it's ok to run in main thread on destroy.

    // Stop the scheduled jobs
    try {
      if (debug) {
        logger.info("Stopping scheduled jobs.");
      }
      this.resetJobTimer(this.updateConfigTimer);
      this.resetJobTimer(this.sendBatchEventTimer);
    } catch (Exception e) {
      // ignore the error.
    }
  }


  /**
   * Method to create scheduled job for sending batch events periodically.
   */
  private void scheduleBatchEventsJob() {
    // Make sure there is none before creating the timer
    this.resetJobTimer(this.sendBatchEventTimer);

    this.sendBatchEventTimer = new Timer("moesif_events_batch_job");
    sendBatchEventTimer.schedule(
        this.batchProcessor,
        0,
        (long) this.batchProcessor.getBatchMaxTime() * 1000
    );
  }

  /**
   * Method to reset scheduled job timer.
   * @param timer : Timer
   */
  private void resetJobTimer(Timer timer) {
    if (timer != null) {
      timer.cancel();
      timer.purge();
      timer = null;
    }
  }

  private void rescheduleSendEventsJobIfNeeded() {

    // Check if batchJob is already running then return
    if (this.batchProcessor.isJobRunning()) {
      if (debug) {
        String msg = String.format("Send event job is in-progress.");
        logger.info(msg);
      }
      return;
    }

    // Check if we need to reschedule job to send batch events
    // if the last job runtime is more than 5 minutes.
    final long MAX_TARDINESS_SEND_EVENT_JOB = this.config.batchMaxTime * 60;      // in seconds
    final long diff = new Date().getTime() - this.batchProcessor.scheduledExecutionTime();
    final long seconds = TimeUnit.MILLISECONDS.toSeconds(diff);

    // Check Event job
    if (seconds > MAX_TARDINESS_SEND_EVENT_JOB) {
      if (debug) {
        String msg = String.format("Last send batchEvents job was executed %d minutes ago. Rescheduling job..", seconds/60);
        logger.info(msg);
      }
      // Restart send batch event job.
      scheduleBatchEventsJob();

    }

    if (debug) {
      String msg = String.format("Last send batchEvents job was executed %d seconds ago.", seconds);
      logger.info(msg);
    }

  }
  /***
   * Method to add event into a queue for batch-based event transfer.
   * The method can be used to just send the batched events, by passing EventModel as null.
   * @param maskedEvent: EventModel
   */
  private void addEventToQueue(EventModel maskedEvent) {
    try {
      this.batchProcessor.addEvent(maskedEvent);
      sendBatchJobAliveCounter++;

      // Check send batchEvent job periodically based on counter if rescheduling is needed.
      if (sendBatchJobAliveCounter > 100) {
        if (this.debug) {
          logger.info("Check for liveness of taskRunner.");
        }
        this.rescheduleSendEventsJobIfNeeded();
        sendBatchJobAliveCounter = 0;
      }

    } catch (Throwable e) {
      logger.warning("Failed to add event to the queue. " + e.toString());
    }
  }

  private EventModel createEvent(EventRequestModel eventRequestModel,
                           String userId,
                           String companyId,
                           String sessionToken,
                           String tags,
                           Object metadata) {

    EventBuilder eb = new EventBuilder();
    eb.request(eventRequestModel);
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
    return eb.build();
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


      try {

        EventModel maskedEvent = config.maskContent(event);
        if (maskedEvent == null) {
          logger.severe("maskContent() returned a null object, not allowed");
        }

        // Generate random number
        double randomPercentage = Math.random() * 100;

        int samplingPercentage = moesifApi.getAPI().getSampleRateToUse(event);

        // Compare percentage to send event
        if (samplingPercentage >= randomPercentage) {
            maskedEvent.setWeight(moesifApi.getAPI().calculateWeight(samplingPercentage));
        	// Add the event to queue for batch-based transfer
            this.addEventToQueue(maskedEvent);
        } 
        else {
        	if(debug) {
        		logger.info("Skipped Event due to SamplingPercentage " + samplingPercentage + " and randomPercentage " + randomPercentage);
        	}
        }

      } catch(Throwable e) {
        if (debug) {
          logger.warning("add event to queue failed " + e);
        }
      }

    } else {
      logger.warning("The application Id should be set before using MoesifFilter");
    }
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