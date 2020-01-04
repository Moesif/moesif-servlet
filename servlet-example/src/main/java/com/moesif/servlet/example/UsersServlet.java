package com.moesif.servlet.example;

import com.moesif.api.APIHelper;
import com.moesif.api.models.CampaignBuilder;
import com.moesif.api.models.CampaignModel;
import com.moesif.api.models.UserBuilder;
import com.moesif.api.models.UserModel;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import com.moesif.servlet.MoesifConfiguration;
import com.moesif.servlet.MoesifFilter;

public class UsersServlet extends HttpServlet {

    private MoesifFilter moesifFilter;

    @Override
    public void init() throws ServletException {
        String applicationId = getInitParameter("application-id");
        moesifFilter = new MoesifFilter();
        moesifFilter.setApplicationId(applicationId);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        if (request.getPathInfo() != null && !request.getPathInfo().isEmpty()) {

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
                    .userId(request.getPathInfo().substring(1).split("/")[0])
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

            try {
                moesifFilter.updateUser(user);
            } catch (Throwable t) {
                System.out.println("Error while updating the user profile.");
            }

            response.setHeader("Content-Type", "application/json");
            response.setStatus(201);
            PrintWriter out = response.getWriter();
            String json = "{"
                    + "\"updated_user\": true"
                    + "}";
            out.println(json);
        }
        else {
            response.setHeader("Content-Type", "application/json");
            response.setStatus(400);
            PrintWriter out = response.getWriter();
            String json = "{"
                    + "\"msg\": \"user_id is not provided.\""
                    + "}";
            out.println(json);
        }
    }
}
