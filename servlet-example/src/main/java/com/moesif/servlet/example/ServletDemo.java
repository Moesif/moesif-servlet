package com.moesif.servlet.example;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ServletDemo extends HttpServlet{

  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException{
    response.setHeader("X-Head-1", "Value1");
    response.setHeader("X-Head-3", "Value3a, Value3b");
    response.setHeader("X-Head-4", "value4");
    response.setHeader("Content-Type", "application/json");
    PrintWriter out = response.getWriter();
    String json = "["
            + "{"
            +   "\"field_b\": \"value1\""
            + "},"
            + "{"
            +   "\"field_b\": \"value2\""
            + "},"
            + "{"
            +   "\"field_b\": \"value3\""
            + "}"
            + "]";

    out.println(json);
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response)
          throws IOException{
    response.setHeader("X-Head-1", "Value1a");
    response.setHeader("X-Head-1", "Value1b");
    response.setHeader("X-Head-3", "Value3a, Value3b");
    response.setHeader("X-Head-4", "value4");
    response.setHeader("Content-Type", "application/json");
    response.setStatus(201);
    PrintWriter out = response.getWriter();
    String json = "{"
            + "\"field_a\": {"
            +     "\"id\": 123456,"
            +     "\"msg\": \"Hello World.\""
            +   "}"
            + "}";
    out.println(json);
  }
}

