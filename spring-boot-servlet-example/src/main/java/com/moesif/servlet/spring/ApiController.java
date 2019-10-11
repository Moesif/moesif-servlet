package com.moesif.servlet.spring;

import java.util.concurrent.atomic.AtomicLong;
import org.springframework.web.bind.annotation.*;


@RestController
public class ApiController {

  private final AtomicLong counter = new AtomicLong();

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
}