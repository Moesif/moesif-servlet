package com.moesif.servlet.jersey.example;

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
import com.moesif.api.models.UserModel;
import com.moesif.api.models.UserBuilder;
import com.moesif.api.models.CampaignModel;
import com.moesif.api.models.CampaignBuilder;
import com.moesif.servlet.MoesifFilter;

/**
 * Root resource (exposed at "users" path)
 */
@Path("users/{id}")
public class UsersServlet {

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
    public Response updateUser(@PathParam("id") String id) throws Throwable {

        // Campaign object is optional, but useful if you want to track ROI of acquisition channels
        // See https://www.moesif.com/docs/api#users for campaign schema
        CampaignModel campaign = new CampaignBuilder()
                .utmSource("google")
                .utmCampaign("cpc")
                .utmMedium("adwords")
                .utmTerm("api+tooling")
                .utmContent("landing")
                .build();

        // Only userId is required
        // metadata can be any custom object
        UserModel user = new UserBuilder()
                .userId(id)
                .companyId("67890") // If set, associate user with a company object
                .campaign(campaign)
                .metadata(APIHelper.deserialize("{" +
                        "\"email\": \"johndoe@acmeinc.com\"," +
                        "\"first_name\": \"John\"," +
                        "\"last_name\": \"Doe\"," +
                        "\"title\": \"Software Engineer\"," +
                        "\"sales_info\": {" +
                        "\"stage\": \"Customer\"," +
                        "\"lifetime_value\": 24000," +
                        "\"account_owner\": \"mary@contoso.com\"" +
                        "}" +
                        "}"))
                .build();

        apiClient.updateUser(user);

        return Response.status(Status.CREATED).entity("{"
                + "\"updated_user\": true"
                + "}").build();
    }
}
