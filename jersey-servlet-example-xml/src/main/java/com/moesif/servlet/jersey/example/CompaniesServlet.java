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
import com.moesif.api.models.CompanyModel;
import com.moesif.api.models.CompanyBuilder;
import com.moesif.api.models.CampaignModel;
import com.moesif.api.models.CampaignBuilder;

/**
 * Root resource (exposed at "companies" path)
 */
@Path("companies/{id}")
public class CompaniesServlet {

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
    public Response updateCompany(@PathParam("id") String id) throws Throwable {

        // Campaign object is optional, but useful if you want to track ROI of acquisition channels
        // See https://www.moesif.com/docs/api#update-a-company for campaign schema
        CampaignModel campaign = new CampaignBuilder()
                .utmSource("google")
                .utmCampaign("cpc")
                .utmMedium("adwords")
                .utmTerm("api+tooling")
                .utmContent("landing")
                .build();

        // Only companyId is required
        // metadata can be any custom object
        CompanyModel company = new CompanyBuilder()
                .companyId("67890")
                .companyDomain("acmeinc.com") // If set, Moesif will enrich your profiles with publicly available info
                .campaign(campaign)
                .metadata(APIHelper.deserialize("{" +
                        "\"org_name\": \"Acme, Inc\"," +
                        "\"plan_name\": \"Free\"," +
                        "\"deal_stage\": \"Lead\"," +
                        "\"mrr\": 24000," +
                        "\"demographics\": {" +
                        "\"alexa_ranking\": 500000," +
                        "\"employee_count\": 47" +
                        "}" +
                        "}"))
                .build();

        apiClient.updateCompany(company);

        return Response.status(Status.CREATED).entity("{"
                + "\"updated_company\": true"
                + "}").build();

    }
}
