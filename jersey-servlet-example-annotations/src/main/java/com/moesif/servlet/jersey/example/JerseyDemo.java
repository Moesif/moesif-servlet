package com.moesif.servlet.jersey.example;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Root resource (exposed at "demo" path)
 */
@Path("demo")
public class JerseyDemo {

    /**
     * Method handling HTTP GET requests. The returned object will be sent
     * to the client as "application/json" media type.
     *
     * @return String that will be returned as a application/json response.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getIt() {
        return  "["
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
    }

    /**
     * Method handling HTTP POST requests. The returned object will be sent
     * to the client as "application/json" media type.
     *
     * @return String that will be returned as a application/json response.
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response postIt() {
        return Response.status(Response.Status.CREATED).entity("{"
                + "\"field_a\": {"
                +     "\"id\": 123456,"
                +     "\"msg\": \"Hello World.\""
                +   "}"
                + "}").build();
    }
}
