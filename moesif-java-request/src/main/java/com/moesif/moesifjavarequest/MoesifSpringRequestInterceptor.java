package com.moesif.moesifjavarequest;

import com.moesif.api.models.*;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import com.moesif.api.MoesifAPIClient;
import com.moesif.api.controllers.APIController;
import com.moesif.api.http.client.APICallBack;
import com.moesif.api.http.client.HttpContext;
import com.moesif.api.http.response.HttpResponse;
import com.moesif.api.IpAddress;
import com.moesif.api.BodyParser;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class MoesifSpringRequestInterceptor implements ClientHttpRequestInterceptor {
    private static final Logger logger = Logger.getLogger(MoesifSpringRequestInterceptor.class.toString());

    private MoesifAPIClient moesifApi;
    private MoesifRequestConfiguration config;

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

    private EventResponseModel buildEventResponseModel(Exception e) {
        return new EventResponseBuilder()
            .time(new Date())
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .body(e.toString())
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
                            warnSendFailed(error);
                        }
                    };

                    api.createEventAsync(eventModel, callback);
                } catch (Throwable e) {
                    warnSendFailed(e);
                }
            }
        }

        if (queryException != null) {
            throw queryException;
        }

        return response;
    }

    private void warnSendFailed(Throwable e) {
        if (config.debug) {
            logger.warning(
                "Error sending event to moesif\n" +
                e.toString() +
                e.getStackTrace()
    );
        }
    }
}

