### Spring Request Tracking

#### Running the Example

This example uses the Request Interceptor to track outging network requests made by a Spring Framework server.

In order to run this example you will need to have Java 7+ and Maven installed.

Before starting, check that your maven version is 3.0.x or above:

```sh
mvn -v
```

1. Clone the repository

	```sh
	git clone https://github.com/Moesif/moesif-servlet
  cd moesif-servlet
	```

2. Update `MoesifHttpClient.java` to use your own Moesif ApplicationId
(Register for an account on [moesif.com](https://www.moesif.com))

3. Compile the example

	```sh
	cd springrequest-example
	mvn clean install
	```
	
4. Run servlet-example

	```sh
	mvn spring-boot:run
	```

5. Go to `http://localhost:8080/create_post` or the port that Tomcat is running on.

In your Moesif Account, you should see API calls logged under API Analytics -> Live Event Stream.

Shut it down manually with Ctrl-C.
