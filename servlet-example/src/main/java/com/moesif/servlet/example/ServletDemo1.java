package com.moesif.servlet.example;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ServletDemo1 extends HttpServlet{

  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException{
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

