package com.moesif.servlet;

public class EnvVars {
    public static String MOESIF_APPLICATION_ID = "MOESIF_APPLICATION_ID";

    public static String readMoesifApplicationId() {
        String appId = System.getenv(MOESIF_APPLICATION_ID);
        if (appId == null || appId.isEmpty()) {
            return "Your Moesif Application Id";
        }
        return appId;
    }
} 