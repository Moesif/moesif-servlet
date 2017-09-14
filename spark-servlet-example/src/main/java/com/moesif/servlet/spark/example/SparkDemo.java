package com.moesif.servlet.spark.example;

import java.util.*;
import java.io.*;
import java.nio.file.*;
import java.nio.charset.*;
import javax.servlet.*;
import javax.servlet.http.*;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;
import spark.servlet.SparkApplication;
import org.apache.commons.io.IOUtils;
import org.apache.commons.fileupload.disk.*;
import org.apache.commons.fileupload.servlet.*;
import org.apache.commons.fileupload.*;
import static spark.Spark.*;

public class SparkDemo implements spark.servlet.SparkApplication {

    @Override
    public void init() {

        staticFiles.externalLocation("/tmp");

        Spark.exception(Exception.class, (e, request, response) -> {
            final StringWriter sw = new StringWriter();
            final PrintWriter pw = new PrintWriter(sw, true);
            e.printStackTrace(pw);
            System.err.println(sw.getBuffer().toString());
        });

        Spark.get("/api/demo",
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

        Spark.post("/api/demo",
            (Request request, Response response) -> {
                return "{"
                    + "\"field_a\": {"
                    + "\"id\": 123456,"
                    + "\"msg\": \"Hello World.\""
                    + "}"
                    + "}";
            });

        Spark.post("/api/upload/file", (req, res) -> {
            final File upload = new File("/tmp");
            if (!upload.exists() && !upload.mkdirs()) {
                throw new RuntimeException("Failed to create directory " + upload.getAbsolutePath());
            }

            // apache commons-fileupload to handle file upload
            DiskFileItemFactory factory = new DiskFileItemFactory();
            factory.setRepository(upload);
            ServletFileUpload fileUpload = new ServletFileUpload(factory);
            List<FileItem> items = fileUpload.parseRequest(req.raw());

            // image is the field name that we want to save
            FileItem item = items.stream()
                    .filter(e -> "file".equals(e.getFieldName()))
                    .findFirst().get();
            String fileName = item.getName();

            item.write(new File(upload, fileName));
            return "File saved at /tmp/" + fileName;
        });
    }
}
