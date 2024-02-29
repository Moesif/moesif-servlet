package com.moesif.servlet.spring;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.Date;
import com.moesif.api.APIHelper;
import com.moesif.api.models.*;
import com.moesif.servlet.MoesifConfiguration;
import com.moesif.servlet.MoesifFilter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;


@RestController
public class ApiController {

  private final AtomicLong counter = new AtomicLong();
  MyConfig config = new MyConfig();
  MoesifFilter moesifFilter = new MoesifFilter(config.applicationId, new MoesifConfiguration(), true);

  @RequestMapping("/")
  public String hello(@RequestParam(value="name", defaultValue="") String name) {
    return "{ \"message\": \"Hello World!\" }";
  }

  @RequestMapping("/api")
  public Greeting greeting(@RequestParam(value="name", defaultValue="") String name) {
    return new Greeting(counter.incrementAndGet(), "Hello There!");
  }

  @GetMapping("gov/no_italy")
  public String noItaly() {
    return "{\"success\" : true}";
  }

  @GetMapping("gov/company1")
  public String company1() {
    return "{\"success\" : true}";
  }

  @GetMapping("gov/canada")
  public String canada() {
    return "{\"success\" : true}";
  }

  @GetMapping("gov/cairo")
  public String cairo() {
    return "{\"success\" : true}";
  }

  @GetMapping("gov/for_companies_in_japan_only")
  public String forCompaniesInJapanOnly() {
    return "{\"success\" : true}";
  }

  @GetMapping("gov/random")
  public String random() {
    return "{\"success\" : true}";
  }

  @GetMapping("gov/multiple_match")
  public String multiple_match() {
    return "{\"success\" : true}";
  }

  @RequestMapping("/api/text")
  @ResponseBody
  public String simpleString() {
    return "this is a simple string";
  }

  @RequestMapping("/api/empty")
  @ResponseBody
  public String emptyBody() {
    return null;
  }

  @RequestMapping("/api/bad")
  @ResponseBody
  public String badJson() {
    return "{[abcdeg";
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
      moesifFilter.updateUser(user);
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
      moesifFilter.updateCompany(company);
    } catch (Throwable t) {
      System.out.println("Error while updating the company profile.");
    }

    return "{ \"update_company\": true }";
  }

  @RequestMapping(value = "/api/subscriptions/{id}", method = RequestMethod.POST)
  @ResponseBody
  @ResponseStatus(code = HttpStatus.CREATED)
  public String updateSubscription(@PathVariable("id") String id) throws IOException {
    // Only subscriptionId is required
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

    return "{ \"update_subscription\": true }";
  }
}