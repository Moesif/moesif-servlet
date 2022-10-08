### Spark Servlet

There are multiple ways to run Spark, as a Java Servlet or embedded with a Java NIO framework like Grizzly. This subsection focuses on running Spark as a Servlet.

Edit the web.xml file to add your application id that you obtained from your Moesif Account.

```xml
  <filter>
    <filter-name>MoesifFilter</filter-name>
    <filter-class>com.moesif.servlet.MoesifFilter</filter-class>
    <init-param>
      <param-name>application-id</param-name>
      <param-value>Your Moesif application id</param-value>
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

#### Running the Spark Servlet example

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
	vim spark-servlet-example/src/main/webapp/WEB-INF/web.xml
	```
  and add it to `new MoesifAPIClient("")`
  ```sh
  vim spark-servlet-example/src/main/java/com/moesif/servlet/spark/example/SparkDemo.java
  ```

3. Compile the example

	```sh
	cd spark-servlet-example
	mvn clean install
	```
	
4. Run spark-servlet-example

	```sh
 	java -jar target/dependency/webapp-runner.jar target/*.war
	```

5. Go to `http://localhost:8080/api/demo` or the port that Tomcat is running on.

In your Moesif Account, you should see API calls logged under API Analytics -> Live Event Stream.

Shut it down manually with Ctrl-C.
