package com.moesif.servlet.spring;
import javax.servlet.Filter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.moesif.servlet.MoesifConfiguration;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.context.annotation.*;
import com.moesif.servlet.MoesifFilter;
import org.springframework.web.servlet.view.InternalResourceViewResolver;


@Configuration
public class MyConfig extends WebMvcConfigurerAdapter {

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

        MoesifFilter moesifFilter = new MoesifFilter("eyJhcHAiOiI2MTc6MTg4IiwidmVyIjoiMi4wIiwib3JnIjoiNjQwOjEyOCIsImlhdCI6MTU3MDc1MjAwMH0.keDf-STVi650VQsbMpPikSEU55UTHX9VHSRkcHPRvcw", config, true);

        // Set flag to log request and response body
        moesifFilter.setLogBody(true);

        return moesifFilter;
    }
}