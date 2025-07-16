package com.moesif.servlet;

import com.moesif.api.MoesifAPIClient;
import org.junit.Test;
import org.mockito.Mockito;

public class UserAgentTest {

    @Test
    public void testUserAgentConfiguration() throws Exception {
        // Mock the MoesifAPIClient
        MoesifAPIClient mockApiClient = Mockito.mock(MoesifAPIClient.class);

        // Test default behavior (no custom user agent)
        MoesifFilter defaultFilter = new MoesifFilter(mockApiClient);
        defaultFilter.setConfigure(new MoesifConfiguration()); // Trigger user agent setting
        String expectedDefaultUserAgent = "moesif-servlet/" + MoesifServletHelper.getVersion();
        Mockito.verify(mockApiClient).setUserAgent(expectedDefaultUserAgent);

        // Test custom user agent via configuration
        MoesifConfiguration customConfig = new MoesifConfiguration();
        String customUserAgent = "MyServletApp/1.0";
        customConfig.userAgent = customUserAgent;
        MoesifFilter customFilter = new MoesifFilter(mockApiClient);
        customFilter.setConfigure(customConfig);
        String expectedCustomUserAgent = expectedDefaultUserAgent + " " + customUserAgent;
        Mockito.verify(mockApiClient).setUserAgent(expectedCustomUserAgent);
    }
} 