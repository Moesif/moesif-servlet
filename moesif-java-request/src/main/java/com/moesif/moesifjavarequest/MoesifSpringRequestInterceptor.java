package com.moesif.moesifjavarequest;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

public class MoesifSpringRequestInterceptor implements ClientHttpRequestInterceptor {
    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        System.out.println("intercept()");
        System.out.println(request.getURI());
        System.out.println(request.getMethod());

//        return clientHttpRequestExecution.execute(httpRequest, bytes);

//        HttpHeaders headers = request.getHeaders();
//        if (!headers.containsKey(HttpHeaders.AUTHORIZATION)) {
//            headers.setBasicAuth(this.username, this.password, this.charset);
//        }
        return execution.execute(request, body);
    }
}

