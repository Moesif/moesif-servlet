package com.moesif.servlet;

import java.io.InputStream;
import java.util.Properties;

public class MoesifServletHelper {

    private static String version = null;

    /**
     * Get the version of the library
     * @return The version
     */
    public synchronized static String getVersion() {
        if (version == null) {
            try {
                Properties p = new Properties();
                InputStream is = null;

                //attempt to load from maven properties first
                try {
                    is = MoesifServletHelper.class.getResourceAsStream(
                            "/META-INF/maven/com.moesif.servlet/moesif-servlet/pom.properties");
                }
                catch(Exception e) {
                    is = null;
                }

                //if not found, fall back to property file
                if(is == null) {
                    is = MoesifServletHelper.class.getResourceAsStream("/moesif-servlet.properties");
                }
                if(is != null) {
                    p.load(is);
                    version = p.getProperty("version", "unknown-version");
                }
            } catch (Exception e) {
                //eat away the exception
            }
        }
        return version;
    }
} 