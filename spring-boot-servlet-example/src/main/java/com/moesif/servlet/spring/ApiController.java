package com.moesif.servlet.spring;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

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
}