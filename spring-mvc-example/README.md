#### Spring MVC example

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

2. Update MyConfig to use your own Moesif ApplicationId
(Register for an account on [moesif.com](https://www.moesif.com))

	```sh
    vim spring-mvc-example/src/main/webapp/WEB-INF/web.xml
	```

3. Run spring-mvc-example

	```sh
	cd spring-mvc-example
	mvn jetty:run
	```

4. Go to `http://localhost:8080/api/json`. In your Moesif Account, you should see event logged and monitored.

Shut it down manually with Ctrl-C.

In Spring MVC + XML configuration, you can register the filters via web.xml

In `web.xml` file:

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
    <init-param>
      <param-name>logBody</param-name>
      <param-value>true</param-value>
    </init-param>
  </filter>
  <filter-mapping>
    <filter-name>MoesifFilter</filter-name>
    <url-pattern>/*</url-pattern>
  </filter-mapping>


```
You may have to override `onStartup()` to pass in the MoesifConfiguration object.