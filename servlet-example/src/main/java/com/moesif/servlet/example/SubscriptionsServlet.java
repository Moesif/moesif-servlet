package com.moesif.servlet.example;

import java.util.Date;

import com.moesif.api.APIHelper;
import com.moesif.api.models.*;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import com.moesif.servlet.MoesifFilter;

public class SubscriptionsServlet extends HttpServlet {

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
            // Only subscriptionId, companyId, and status
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

            try {
                moesifFilter.updateSubscription(subscription);
            } catch (Throwable t) {
                System.out.println("Error while updating the subscription profile.");
            }

            response.setHeader("Content-Type", "application/json");
            response.setStatus(201);
            PrintWriter out = response.getWriter();
            String json = "{"
                    + "\"updated_subscription\": true"
                    + "}";
            out.println(json);
        } else {
            response.setHeader("Content-Type", "application/json");
            response.setStatus(400);
            PrintWriter out = response.getWriter();
            String json = "{"
                    + "\"msg\": \"subscription_id, company_id, and status are required.\""
                    + "}";
            out.println(json);
        }
    }
}

