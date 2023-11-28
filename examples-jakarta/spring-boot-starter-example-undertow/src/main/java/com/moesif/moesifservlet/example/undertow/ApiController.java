package com.moesif.moesifservlet.example.undertow;

import com.moesif.servlet.MoesifConfiguration;
import com.moesif.servlet.MoesifFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.atomic.AtomicLong;

@RestController
public class ApiController {

    private final AtomicLong counter = new AtomicLong();
    MyConfig config = new MyConfig();
    MoesifFilter moesifFilter = new MoesifFilter(config.applicationId, new MoesifConfiguration(), true);

     @RequestMapping("/")
     public String hello(@RequestParam(value="name", defaultValue="") String name) {
       return "{ \"message\": \"Hello Moesif!\" }";
     }
}
