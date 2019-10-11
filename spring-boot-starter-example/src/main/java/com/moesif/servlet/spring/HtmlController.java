package com.moesif.servlet.spring;

import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.logging.Logger;


@Controller
public class HtmlController {
  private static final Logger logger = Logger.getLogger(ApiController.class.toString());

  @RequestMapping("/html_string")
  @ResponseBody
  public String html() {
    return "<html><body>hello</body></html>";
  }

  @RequestMapping("/html_view")
  public ModelAndView html(Model model) {
    model.addAttribute("message", "Hello World!");
    return new ModelAndView("index");
  }

  @RequestMapping("/index.html")
  public String home(Map<String, Object> model) {
    model.put("message", "Hello World!");
    return "index";
  }
}