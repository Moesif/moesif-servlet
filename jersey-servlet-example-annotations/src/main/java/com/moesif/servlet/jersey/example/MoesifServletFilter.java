package com.moesif.servlet.jersey.example;

import com.moesif.servlet.MoesifConfiguration;
import com.moesif.servlet.MoesifFilter;

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebFilter;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.logging.Logger;

@WebFilter(
    urlPatterns ="/*",
    initParams = {
        @WebInitParam(name = "application-id",
            value = "Your Moesif Application Id"),
        @WebInitParam(name = "logBody", value = "true")
    }
)
public class MoesifServletFilter extends MoesifFilter {
    Logger log = Logger.getLogger(MoesifServletFilter.class.getName());

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        setDebug(true);

        // Use custom logic to determine User ID and other event metadata from requests
        MoesifConfiguration config = new MoesifConfiguration() {
            @Override
            public String identifyUser(HttpServletRequest request, HttpServletResponse response) {
                return "demo-user";
            }

            @Override
            public String identifyCompany(HttpServletRequest request, HttpServletResponse response) {
                return "demo-company";
            }

            @Override
            public String getSessionToken(HttpServletRequest request, HttpServletResponse response) {
                return "demo-session";
            }

            @Override
            public String getApiVersion(HttpServletRequest request, HttpServletResponse response) {
                return "1.0.0";
            }
        };
        setConfigure(config);
        super.init(filterConfig);
        log.info("MoesifFilter initialized with " + this.getAPI());
    }
}