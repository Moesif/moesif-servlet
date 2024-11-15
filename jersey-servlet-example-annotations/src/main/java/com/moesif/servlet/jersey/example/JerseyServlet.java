package com.moesif.servlet.jersey.example;

import javax.servlet.annotation.WebServlet;
import javax.servlet.annotation.WebInitParam;
import org.glassfish.jersey.servlet.ServletContainer;

@WebServlet(
    urlPatterns = {"/api/*"}, // Match your desired URL pattern
    initParams = {
        @WebInitParam(
            name = "javax.ws.rs.Application",
            value = "com.moesif.servlet.jersey.example.MoesifJerseyApplication"
        )
    },
    loadOnStartup = 1
)
public class JerseyServlet extends ServletContainer {
    // No additional code needed here
}
