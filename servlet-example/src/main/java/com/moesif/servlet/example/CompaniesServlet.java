package com.moesif.servlet.example;

import com.moesif.api.APIHelper;
import com.moesif.api.models.*;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import com.moesif.servlet.MoesifFilter;


public class CompaniesServlet extends HttpServlet {

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
                    .companyId(request.getPathInfo().substring(1).split("/")[0])
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

            try {
                moesifFilter.updateCompany(company);
            } catch (Throwable t) {
                System.out.println("Error while updating the company profile.");
            }

            response.setHeader("Content-Type", "application/json");
            response.setStatus(201);
            PrintWriter out = response.getWriter();
            String json = "{"
                    + "\"updated_company\": true"
                    + "}";
            out.println(json);
        }
        else {
            response.setHeader("Content-Type", "application/json");
            response.setStatus(400);
            PrintWriter out = response.getWriter();
            String json = "{"
                    + "\"msg\": \"company_id is not provided.\""
                    + "}";
            out.println(json);
        }
    }
}
