package com.moesif.servlet.spark.example;

import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;
import spark.servlet.SparkApplication;

public class SparkDemo implements spark.servlet.SparkApplication {

    @Override
    public void init() {

        Spark.get("/demo",
           (Request request, Response response) -> {
                return "["
                    + "{"
                    + "\"field_b\": \"value1\""
                    + "},"
                    + "{"
                    + "\"field_b\": \"value2\""
                    + "},"
                    + "{"
                    + "\"field_b\": \"value3\""
                    + "}"
                    + "]";
           });

        Spark.post("/demo",
            (Request request, Response response) -> {
                return "{"
                    + "\"field_a\": {"
                    + "\"id\": 123456,"
                    + "\"msg\": \"Hello World.\""
                    + "}"
                    + "}";
            });
    }
}
