package com.moesif.servlet.spring;

import java.util.concurrent.atomic.AtomicLong;

import org.springframework.web.bind.annotation.*;

import java.util.logging.Logger;


@RestController
public class GreetingController {

  private static final String template = "Hello, %s! You are %s";
  private final AtomicLong counter = new AtomicLong();

  private static final Logger logger = Logger.getLogger(GreetingController.class.toString());

  @RequestMapping("/greeting")
  public Greeting greeting(@RequestParam(value="name", defaultValue="World") String name,
                           @RequestParam(value="age", defaultValue="5") String age) {
    return new Greeting(counter.incrementAndGet(), String.format(template, name, age));
  }

  @RequestMapping("/hello")
  public Greeting hello(@RequestParam(value="name", defaultValue="") String name) {
    return new Greeting(counter.incrementAndGet(), String.format("There you are %s", name));
  }

  @RequestMapping("/simplestring")
  @ResponseBody
  public String simpleString() {
    return "this is a simple string";
  }

  @RequestMapping("/empty_resp_body")
  @ResponseBody
  public String emptyRespBody(@RequestParam(value="name", defaultValue="") String name, @RequestBody String reqBody) {
    return reqBody;
  }

  @RequestMapping("/badjson")
  @ResponseBody
  public String badJson() {
    return "{[abcdeg";
  }

  @RequestMapping("/html")
  @ResponseBody
  public String html() {
    return "<html><body>hello</body></html>";
  }
}