package com.moesif.springrequest;

import com.moesif.api.models.EventModel;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpResponse;

public class MoesifRequestConfiguration {
    /**
     * Enable debug error reporting via console
     */
    public boolean debug = false;

    /**
     * Return true if and only if the request should not be recorded to moesif
     * @param request the HTTP request
     * @param response the HTTP response
     * @return true if and only if the request should not be logged
     */
    public boolean skip(HttpRequest request, ClientHttpResponse response) {
        return false;
    }

    /**
     * Allows data to be filtered
     * eg: remove headers, mask credit card numbers
     * @param eventModel event to be masked
     * @return event to be sent
     */
    public EventModel maskContent(EventModel eventModel) {
        return eventModel;
    }

    /**
     * If Moesif is unable to identify the user, custom logic can be defined here
     * @param request the HTTP request
     * @param response the HTTP response
     * @return the user ID
     */
    public String identifyUser(HttpRequest request, ClientHttpResponse response) {
        return null;
    }

    /**
     * Provide the companyId
     * @param request the HTTP request
     * @param response the HTTP response
     * @return the company ID
     */
    public String identifyCompany(HttpRequest request, ClientHttpResponse response) {
        return null;
    }

    /**
     * If Moesif is unable to identify the session, custom logic can be defined here
     * @param request the HTTP request
     * @param response the HTTP response
     * @return the Session Token
     */
    public String getSessionToken(HttpRequest request, ClientHttpResponse response) {
        return null;
    }

    /**
     * Provide an API version
     * @param request the HTTP request
     * @param response the HTTP response
     * @return the API Version
     */
    public String getApiVersion(HttpRequest request, ClientHttpResponse response) {
        return null;
    }

    /**
     * Provide any extra attributes that should be associated with this request
     * @param request the HTTP request
     * @param response the HTTP response
     * @return the metadata
     */
    public Object getMetadata(HttpRequest request, ClientHttpResponse response) {
        return null;
    }
}
