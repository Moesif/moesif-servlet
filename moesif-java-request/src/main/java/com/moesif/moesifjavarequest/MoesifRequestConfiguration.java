package com.moesif.moesifjavarequest;

import com.moesif.api.models.EventModel;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpResponse;

public class MoesifRequestConfiguration {
    public boolean skip(HttpRequest request, ClientHttpResponse response) {
        return false;
    }

    public EventModel maskContent(EventModel eventModel) {
        return eventModel;
    }

    public String identifyUser(HttpRequest request, ClientHttpResponse response) {
        return null;
    }

    public String getSessionToken(HttpRequest request, ClientHttpResponse response) {
        return null;
    }

    public String getApiVersion(HttpRequest request, ClientHttpResponse response) {
        return null;
    }

    public Object getMetadata(HttpRequest request, ClientHttpResponse response) {
        return null;
    }
}
