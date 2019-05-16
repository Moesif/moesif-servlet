package com.moesif.javarequestexample;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class PostController {

  private MoesifHttpClient httpClient = new MoesifHttpClient();

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
}