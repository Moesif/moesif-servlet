package com.moesif.servlet.spark.example;

import java.util.*;
import java.io.*;
import com.moesif.api.APIHelper;
import com.moesif.api.MoesifAPIClient;
import com.moesif.api.controllers.APIController;
import com.moesif.api.models.*;
import spark.Request;
import spark.Response;
import spark.Spark;
import org.apache.commons.fileupload.disk.*;
import org.apache.commons.fileupload.servlet.*;
import org.apache.commons.fileupload.*;
import static spark.Spark.staticFiles;

public class SparkDemo implements spark.servlet.SparkApplication {

    private APIController apiClient;

    @Override
    public void init() {

        apiClient = new MoesifAPIClient("Your Moesif Application Id").getAPI();

        staticFiles.externalLocation("/tmp");

        Spark.exception(Exception.class, (e, request, response) -> {
            final StringWriter sw = new StringWriter();
            final PrintWriter pw = new PrintWriter(sw, true);
            e.printStackTrace(pw);
            System.err.println(sw.getBuffer().toString());
        });

        Spark.get("/api/demo",
           (Request request, Response response) -> {
                return "["
                    + "{"
                    + "\"field_b\": \"value1\""
                    + "},"
                    + "{"
                    + "\"field_b\": \"value2\""
                    + "},"
                    + "{"
                    + "\"field_b\": \"value3\""
                    + "}"
                    + "]";
           });

        Spark.post("/api/demo",
            (Request request, Response response) -> {
                response.status(201);
                return "{"
                    + "\"field_a\": {"
                    + "\"id\": 123456,"
                    + "\"msg\": \"Hello World.\""
                    + "}"
                    + "}";
            });

        Spark.post("/api/users/:id",
            (Request request, Response response) -> {

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
                        .userId(request.params(":id"))
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
                    apiClient.updateUser(user);
                } catch (Throwable t) {
                    System.out.println("Error while updating the user profile.");
                }

                response.header("Content-Type", "application/json");
                response.status(201);
                return "{"
                + "\"updated_user\": true"
                + "}";
            }
        );


        Spark.post("/api/companies/:id",
            (Request request, Response response) -> {

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
                        .companyId(request.params(":id"))
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
                    apiClient.updateCompany(company);
                } catch (Throwable t) {
                    System.out.println("Error while updating the company profile.");
                }

                response.header("Content-Type", "application/json");
                response.status(201);
                return "{"
                        + "\"updated_company\": true"
                        + "}";
            }
        );


        Spark.post("/api/upload/file", (req, res) -> {
            final File upload = new File("/tmp");
            if (!upload.exists() && !upload.mkdirs()) {
                throw new RuntimeException("Failed to create directory " + upload.getAbsolutePath());
            }

            // apache commons-fileupload to handle file upload
            DiskFileItemFactory factory = new DiskFileItemFactory();
            factory.setRepository(upload);
            ServletFileUpload fileUpload = new ServletFileUpload(factory);
            List<FileItem> items = fileUpload.parseRequest(req.raw());

            // image is the field name that we want to save
            FileItem item = items.stream()
                    .filter(e -> "file".equals(e.getFieldName()))
                    .findFirst().get();
            String fileName = item.getName();

            item.write(new File(upload, fileName));
            return "File saved at /tmp/" + fileName;
        });
    }
}
