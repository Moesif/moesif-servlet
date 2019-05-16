package com.moesif.javarequest;

import com.moesif.api.models.*;

import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.HttpServerErrorException.GatewayTimeout;

import com.moesif.api.MoesifAPIClient;
import com.moesif.api.controllers.APIController;
import com.moesif.api.http.client.APICallBack;
import com.moesif.api.http.client.HttpContext;
import com.moesif.api.http.response.HttpResponse;
import com.moesif.api.IpAddress;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.moesif.api.BodyParser;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class MoesifSpringRequestInterceptor implements ClientHttpRequestInterceptor {
    private static final Logger logger = Logger.getLogger(MoesifSpringRequestInterceptor.class.toString());

    private MoesifAPIClient moesifApi;
    private MoesifRequestConfiguration config;

    private int ERROR_GATEWAY_TIMEOUT = 504;
    private int ERROR_BAD_GATEWAY = 502;

    public MoesifSpringRequestInterceptor(MoesifAPIClient moesifApi) {
        this.moesifApi = moesifApi;
        this.config = new MoesifRequestConfiguration();

        this.moesifApi.getAPI().setShouldSyncAppConfig(true);
    }

    public MoesifSpringRequestInterceptor(String applicationId) {
        this.moesifApi = new MoesifAPIClient(applicationId);
        this.config = new MoesifRequestConfiguration();

        this.moesifApi.getAPI().setShouldSyncAppConfig(true);
    }

    public MoesifSpringRequestInterceptor(MoesifAPIClient moesifApi, MoesifRequestConfiguration config) {
        this(moesifApi);
        this.config = config;
    }

    public MoesifSpringRequestInterceptor(String applicationId, MoesifRequestConfiguration config) {
        this(applicationId);
        this.config = config;
    }

    private EventRequestModel buildEventRequestModel(HttpRequest request, byte[] body) {
        EventRequestBuilder eventRequestBuilder = new EventRequestBuilder();
        Map<String, String> headers = new HashMap<String, String>(0);
        eventRequestBuilder
                .time(new Date())
                .uri(request.getURI().toString())
                .headers(request.getHeaders().toSingleValueMap())
                .verb(request.getMethod().toString())
                .ipAddress(IpAddress.getClientIp(
                    request.getHeaders().toSingleValueMap(),
                    null
                ));

        String content = new String(body);
        if (content != null && !content.isEmpty()) {
            BodyParser.BodyWrapper bodyWrapper = BodyParser.parseBody(headers, content);
            eventRequestBuilder.body(bodyWrapper.body);
            eventRequestBuilder.transferEncoding(bodyWrapper.transferEncoding);
        }


        return eventRequestBuilder.build();
    }

    private EventResponseModel buildEventResponseModel(MoesifClientHttpResponse response) throws IOException {
        EventResponseBuilder eventResponseBuilder = new EventResponseBuilder();
        Map<String, String> headers = response.getHeaders().toSingleValueMap();

        int statusCode = 0;

        try {
            statusCode = response.getRawStatusCode();
        } catch (Exception e) {
            logger.warning(e.toString());
        }

        eventResponseBuilder
                .time(new Date())
                .status(statusCode)
                .headers(headers);

        String content = response.getBodyString();

        if (content != null && !content.isEmpty()) {
            BodyParser.BodyWrapper bodyWrapper = BodyParser.parseBody(headers, content);
            eventResponseBuilder.body(bodyWrapper.body);
            eventResponseBuilder.transferEncoding(bodyWrapper.transferEncoding);
        }


        return eventResponseBuilder.build();
    }

    private int getStatusCodeForError(Exception e) {
        int result = ERROR_BAD_GATEWAY;

        if (e.getClass().getName().equals("org.springframework.web.client.HttpServerErrorException.GatewayTimeout")) {
            result = ERROR_GATEWAY_TIMEOUT;
        }

        return result;
    }

    private EventResponseModel buildEventResponseModel(Exception e) {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.createObjectNode();
        JsonNode error = mapper.createObjectNode();

        ((ObjectNode) error).put("code", "moesif_java_request_error");
        ((ObjectNode) error).put("msg", e.toString());
        ((ObjectNode) rootNode).set("moesif_error", error);

        String response = null;
        try {
            response = mapper
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(rootNode);
        } catch (JsonProcessingException ex) {
            warn(ex);
        }

        return new EventResponseBuilder()
            .time(new Date())
            .status(getStatusCodeForError(e))
            .body(response)
            .headers(new HashMap<String, String>()) // required
            .build();
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        IOException queryException = null;
        EventRequestModel eventRequestModel = buildEventRequestModel(request, body);
        EventResponseModel eventResponseModel = null;

        MoesifClientHttpResponse response = null;
        try {
            // run the request
            ClientHttpResponse rawResponse = execution.execute(request, body);

            response = new MoesifClientHttpResponse(rawResponse);
        } catch (IOException e) {
            queryException = e;
        }

        if (queryException == null) {
            // wrap it so we can read the body twice
            eventResponseModel = buildEventResponseModel(response);
        } else {
            eventResponseModel = buildEventResponseModel(queryException);
        }

        if (!config.skip(request, response)) {
            APIController api = moesifApi.getAPI();

            EventModel eventModel = api.buildEventModel(
                eventRequestModel,
                eventResponseModel,
                config.identifyUser(request, response),
                config.getSessionToken(request, response),
                config.getApiVersion(request, response),
                config.getMetadata(request, response)
            );

            eventModel = config.maskContent(eventModel);

            if (api.shouldSendSampledEvent()) {
                try {
                    APICallBack<HttpResponse> callback = new APICallBack<HttpResponse>() {
                        public void onSuccess(HttpContext context, HttpResponse response) {
                            // noop
                        }

                        public void onFailure(HttpContext context, Throwable error) {
                            warn(error);
                        }
                    };

                    api.createEventAsync(eventModel, callback);
                } catch (Throwable e) {
                    warn(e);
                }
            }
        }

        if (queryException != null) {
            throw queryException;
        }

        return response;
    }

    private void warn(Throwable e) {
        if (config.debug) {
            logger.warning("Warning:\n" + e.toString());
        }
    }
}

