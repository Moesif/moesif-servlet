package com.moesif.moesifjavarequest;

import com.moesif.api.models.*;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import com.moesif.api.MoesifAPIClient;
import com.moesif.api.controllers.APIController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MoesifSpringRequestInterceptor implements ClientHttpRequestInterceptor {
    private MoesifAPIClient moesifApi;
    private MoesifRequestConfiguration config;

    public MoesifSpringRequestInterceptor(MoesifAPIClient moesifApi) {
        this.moesifApi = moesifApi;
        this.config = new MoesifRequestConfiguration();
    }

    public MoesifSpringRequestInterceptor(String applicationId) {
        this.moesifApi = new MoesifAPIClient(applicationId);
        this.config = new MoesifRequestConfiguration();
    }

    public MoesifSpringRequestInterceptor(MoesifAPIClient moesifApi, MoesifRequestConfiguration config) {
        this(moesifApi);
        this.config = config;
    }

    public MoesifSpringRequestInterceptor(String applicationId, MoesifRequestConfiguration config) {
        this(applicationId);
        this.config = config;
    }

    private String streamToString(InputStream inputStream) {
        try {
            StringBuilder inputStringBuilder = new StringBuilder();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            String line = bufferedReader.readLine();

            while (line != null) {
                inputStringBuilder.append(line);
                inputStringBuilder.append('\n');
                line = bufferedReader.readLine();
            }

            return inputStringBuilder.toString();
        } catch (IOException e) {
            return "";
        }
    }

    private EventRequestModel buildEventRequestModel(HttpRequest request, byte[] body) {
        EventRequestBuilder eventRequestBuilder = new EventRequestBuilder();
        Map<String, String> reqHeaders = new HashMap<String, String>(0);
        eventRequestBuilder
                .time(new Date())
                .uri(request.getURI().toString())
                .headers(request.getHeaders().toSingleValueMap())
                .verb(request.getMethod().toString());

        eventRequestBuilder.body(new String(body));

        return eventRequestBuilder.build();
    }

    private EventResponseModel buildEventResponseModel(ClientHttpResponse response) {
        EventResponseBuilder eventResponseBuilder = new EventResponseBuilder();

        int statusCode = 0;

        try {
            statusCode = response.getRawStatusCode();
        } catch (Exception e) {
            System.out.println("Error getting raw status code");
        }

        eventResponseBuilder
                .time(new Date())
                .status(statusCode)
                .headers(response.getHeaders().toSingleValueMap());

        try {
            eventResponseBuilder.body(streamToString(response.getBody()));
        } catch (Exception e) {
            System.out.println("Error getting response body");
        }

        return eventResponseBuilder.build();
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        AppConfigModel appConfig = moesifApi.getAPI().getCachedAndLoadAppConfig();
        
        EventRequestModel eventRequestModel = buildEventRequestModel(request, body);

        // run the request
        ClientHttpResponse response = execution.execute(request, body);

        EventResponseModel eventResponseModel = buildEventResponseModel(response);

        if (!config.skip(request, response)) {
            APIController api = moesifApi.getAPI();

            EventModel eventModel = api.buildEventModel(
                eventRequestModel,
                eventResponseModel,
                config.identifyUser(request, response),
                config.getSessionToken(request, response),
                null,
                config.getMetadata(request, response)
            );

            eventModel = config.maskContent(eventModel);

            if (api.shouldSendSampledEvent()) {
                try {
                    api.createEvent(eventModel);
                } catch (Throwable e) {
                    // TODO: log?
                }
            }
        }

        return response;
    }
}

