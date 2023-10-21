package com.moesif.servlet.spring;
import javax.servlet.Filter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.moesif.servlet.MoesifConfiguration;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.context.annotation.*;
import com.moesif.servlet.MoesifFilter;


@Configuration
public class MyConfig implements WebMvcConfigurer {

    public String applicationId = "Your Moesif Application Id";
    @Bean
    public Filter moesifFilter() {

        MoesifConfiguration config = new MoesifConfiguration() {

            @Override
            public String identifyUser(HttpServletRequest request, HttpServletResponse response) {
                if (request.getUserPrincipal() == null) {
                    return null;
                }
                return request.getUserPrincipal().getName();
            }

            @Override
            public String getSessionToken(HttpServletRequest request, HttpServletResponse response) {
                return request.getHeader("Authorization");
            }

            @Override
            public String getApiVersion(HttpServletRequest request, HttpServletResponse response) {
                return request.getHeader("X-Api-Version");
            }
        };

        MoesifFilter moesifFilter = new MoesifFilter(applicationId, config, true);

        // Set flag to log request and response body
        moesifFilter.setLogBody(true);

        return moesifFilter;
    }
}