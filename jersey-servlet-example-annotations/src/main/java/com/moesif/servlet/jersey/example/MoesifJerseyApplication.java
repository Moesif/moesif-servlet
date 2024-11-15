package com.moesif.servlet.jersey.example;

import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;

import java.util.logging.Logger;


public class MoesifJerseyApplication extends ResourceConfig {
    Logger log = Logger.getLogger(MoesifJerseyApplication.class.getName());

    public MoesifJerseyApplication() {
        log.info("Registering MoesifJerseyApplication");
        packages("com.moesif.servlet.jersey.example");
        register(MultiPartFeature.class);
    }
}