package com.moesif.javarequest;

import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class PostController {

  private MoesifHttpClient httpClient = new MoesifHttpClient();

  @RequestMapping("/create_post")
  public String createPost(@RequestParam(value="name", defaultValue="New Post") String name) {
    JSONObject json = new JSONObject();

    json.put("id", Long.toString(Math.round(Math.random() * 1000)).toString());
    json.put("name", name);

    HttpEntity<String> request = new HttpEntity<String>(json.toString());

    ResponseEntity<String> response = httpClient.getRestTemplate().exchange(
      "https://jsonplaceholder.typicode.com/posts",
      HttpMethod.POST,
      request,
      String.class
    );

    return response.getBody();
  }
}