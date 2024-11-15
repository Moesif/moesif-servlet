package com.moesif.servlet.jersey.example;

import java.util.Date;

import javax.annotation.PostConstruct;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import com.moesif.api.MoesifAPIClient;
import com.moesif.api.APIHelper;
import com.moesif.api.controllers.APIController;
import com.moesif.api.models.SubscriptionModel;
import com.moesif.api.models.SubscriptionBuilder;

/**
 * Root resource (exposed at "subscriptions" path)
 */
@Path("subscriptions/{id}")
public class SubscriptionsServlet {

    private APIController apiClient;

    @Context
    private Configuration config;

    @PostConstruct
    public void init() {
        String applicationId = (String) config.getProperty("application-id");
        apiClient = new MoesifAPIClient(applicationId).getAPI();
    }

    /**
     * Method handling HTTP POST requests. The returned object will be sent
     * to the client as "application/json" media type.
     *
     * @return Response that will be returned as an application/json.
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateSubscription(@PathParam("id") String id) throws Throwable {

        // Only subscriptionId, companyId, and status are required
        // metadata can be any custom object
        SubscriptionModel subscription = new SubscriptionBuilder()
                .subscriptionId("sub_12345")
                .companyId("67890")
                .currentPeriodStart(new Date())
                .currentPeriodEnd(new Date())
                .status("active")
                .metadata(APIHelper.deserialize("{" +
                        "\"email\": \"johndoe@acmeinc.com\"," +
                        "\"string_field\": \"value_1\"," +
                        "\"number_field\": 0," +
                        "\"object_field\": {" +
                        "\"field_1\": \"value_1\"," +
                        "\"field_2\": \"value_2\"" +
                        "}" +
                        "}"))
                .build();

        apiClient.updateSubscription(subscription);

        return Response.status(Status.CREATED).entity("{"
                + "\"updated_company\": true"
                + "}").build();
    }
}
