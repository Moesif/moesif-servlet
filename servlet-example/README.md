### Generic Java Servlet

Edit the web.xml file to add your application id that you obtained from your Moesif Account.

```xml
  <filter>
    <filter-name>MoesifFilter</filter-name>
    <filter-class>com.moesif.servlet.MoesifFilter</filter-class>
    <init-param>
      <param-name>application-id</param-name>
      <param-value>your application id</param-value>
    </init-param>
    <init-param>
      <param-name>debug</param-name>
      <param-value>false</param-value>
    </init-param>
  </filter>
  <filter-mapping>
    <filter-name>MoesifFilter</filter-name>
    <url-pattern>/*</url-pattern>
  </filter-mapping>

```

#### Running the Generic Servlet example

This example implements the Servlet Filter directly in a generic Servlet app rather than using a higher level
framework like Spring MVC or Spring Boot.

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

2. Update web.xml to use your own Moesif ApplicationId
(Register for an account on [moesif.com](https://www.moesif.com))

	```sh
	vim servlet-example/src/main/webapp/WEB-INF/web.xml
	```

3. Run servlet-example

	```sh
	cd servlet-example
	mvn tomcat7:run
	```

4. Go to `http://localhost:3099/api/demo` or the port that Tomcat is running on.

In your Moesif Account, you should see event logged and monitored.

Shut it down manually with Ctrl-C.
