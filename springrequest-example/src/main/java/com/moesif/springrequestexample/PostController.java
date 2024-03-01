package com.moesif.springrequestexample;

import com.moesif.api.APIHelper;
import com.moesif.api.MoesifAPIClient;
import com.moesif.api.controllers.APIController;
import com.moesif.api.models.*;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.util.Date;

@RestController
public class PostController {

  private MoesifHttpClient httpClient = new MoesifHttpClient();
  private APIController apiClient = new MoesifAPIClient(httpClient.applicationId).getAPI();

  @RequestMapping("/create_post")
  public String createPost(@RequestParam(value="name", defaultValue="New Post") String name) {
    HttpEntity<String> request = new HttpEntity<String>("{\"id\": \"1\", \"name\": \"new post\"}");

    ResponseEntity<String> response = httpClient.getRestTemplate().exchange(
      "https://jsonplaceholder.typicode.com/posts",
      HttpMethod.POST,
      request,
      String.class
    );

    return response.getBody();
  }

  @RequestMapping(value = "/api/users/{id}", method = RequestMethod.POST)
  @ResponseBody
  @ResponseStatus(code = HttpStatus.CREATED)
  public String updateUser(@PathVariable("id") String id) throws IOException {
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

    try {
      apiClient.updateUser(user);
    } catch (Throwable t) {
      System.out.println("Error while updating the user profile.");
    }

    return "{ \"update_user\": true }";
  }

  @RequestMapping(value = "/api/companies/{id}", method = RequestMethod.POST)
  @ResponseBody
  @ResponseStatus(code = HttpStatus.CREATED)
  public String updateCompany(@PathVariable("id") String id) throws IOException {
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
            .companyId(id)
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

    return "{ \"update_company\": true }";
  }

  @RequestMapping(value = "/api/subscriptions/{id}", method = RequestMethod.POST)
  @ResponseBody
  @ResponseStatus(code = HttpStatus.CREATED)
  public String updateSubscription(@PathVariable("id") String id) throws IOException {
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

    try {
      apiClient.updateSubscription(subscription);
    } catch (Throwable t) {
      System.out.println("Error while updating the subscription profile.");
    }

    return "{ \"update_subscription\": true }";
  }
}