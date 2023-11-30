# Moesif Servlet SDK

 [![Built For][ico-built-for]][link-built-for]
 [![Latest Version][ico-version]][link-package]
 [![Software License][ico-license]][link-license]
 [![Source Code][ico-source]][link-source]

## Introduction

`moesif-servlet` is a Java Servlet Filter that logs _incoming_ API calls and sends to [Moesif](https://www.moesif.com) for API analytics and monitoring.

The SDK is implemented as a [JavaX Servlet Filter](https://tomcat.apache.org/tomcat-5.5-doc/servletapi/javax/servlet/Filter.html)
without importing framework specific dependencies. Any framework built on Java [Servlet API](https://tomcat.apache.org/tomcat-5.5-doc/servletapi/javax/servlet/Servlet.html) such as Spring, Struts, Jersey, etc can use this SDK with minimal configuration.


`moesif-servlet-jakarta` is an identical implementation that uses the newer [Jakarta Servlet API](https://tomcat.apache.org/tomcat-10.0-doc/servletapi/jakarta/servlet/Servlet.html) and works with Java 17+ [Tomcat 10](https://tomcat.apache.org/tomcat-10.0-doc/index.html) and [Spring Boot 3.0](https://spring.io/projects/spring-boot). The source code is located in folder[`moesif-servlet-jakarta`](moesif-servlet-jakarta)

[Source Code on GitHub](https://github.com/moesif/moesif-servlet)

## How to install

#### Maven users

Add the Moesif dependency to your project's pom.xml file:

```xml
<dependency>
    <groupId>com.moesif.servlet</groupId>
    <artifactId>moesif-servlet</artifactId>
    <version>1.7.10</version>
</dependency>

<!-- OR for newer Jakarta-->
<dependency>
    <groupId>com.moesif.servlet</groupId>
    <artifactId>moesif-servlet-jakarta</artifactId>
    <version>2.1.1</version>
</dependency>
```

#### Gradle users

Add the Moesif dependency to your project's build.gradle file:

```gradle
dependencies {   
    compile 'com.moesif.servlet:moesif-servlet:1.7.9'
}

// OR for newer Jakarta
dependencies {   
    compile 'com.moesif.servlet:moesif-servlet-jakarta:2.0.3'
}
```

## How to use

Different Java web frameworks have different way of configuring filters. 
Go to your specific framework's instructions below:

- [Spring Boot](#spring-boot)
- [Spring MVC](#spring-mvc-java-config)
- [Jersey Servlet](#jersey-servlet)
- [Spark Servlet](#spark-servlet)
- [Generic Java Servlet](#generic-java-servlet)
- Spring Boot 3.x using Jakarta
  - [Spring Boot 3.0 Jakarta with Tomcat](https://github.com/Moesif/moesif-servlet/tree/master/examples-jakarta/spring-boot-starter-example-tomcat)
  - [Spring Boot 3.2 Jakarta with Undertow](https://github.com/Moesif/moesif-servlet/tree/master/examples-jakarta/spring-boot-starter-example-undertow)


Your Moesif Application Id can be found in the [_Moesif Portal_](https://www.moesif.com/).
After signing up for a Moesif account, your Moesif Application Id will be displayed during the onboarding steps. 

You can always find your Moesif Application Id at any time by logging 
into the [_Moesif Portal_](https://www.moesif.com/), click on the top right menu,
and then clicking _Installation_.

### Spring Boot

In your Spring configuration file, install the Moesif Filter object.

```java

import com.moesif.servlet.MoesifFilter;

import javax.servlet.Filter;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.context.annotation.*;
import org.springframework.http.converter.*;

@Configuration
public class MyConfig implements WebMvcConfigurer {

  @Bean
  public Filter moesifFilter() {
    return new MoesifFilter("Your Moesif Application Id");
  }
}
```

To customize the filter, pass in a object that implements `MoesifConfiguration` such
as `MoesifConfigurationAdapter`
For details regarding `MoesifConfiguration`, see the [configuration options](#configuration-options).

```java
@Configuration
public class MyConfig implements WebMvcConfigurer {

  MoesifConfiguration config = new MoesifConfigurationAdapter() {
    @Override
    public String getSessionToken(HttpServletRequest request, HttpServletResponse response) {
      return request.getHeader("Authorization");
    }
  };

  @Bean
  public Filter moesifFilter() {
    return new MoesifFilter("Your Moesif Application Id", config);
  }
}
```

#### Running the Spring Boot example

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
	vim spring-boot-starter-example/src/main/java/com/moesif/servlet/spring/MyConfig.java
	```

3. Compile the example

	```sh
	cd spring-boot-starter-example
	mvn clean install
	```

4. Run it

	```sh
	java -jar target/spring-boot-starter-example*.jar
	```

	Alternatively:

	```sh
	mvn  spring-boot:run
	```


5. Using Postman or CURL, make a few API calls to `http://localhost:8080/api` or the port that Spring Boot is running on.
   
6. Verify the API calls are logged to your [Moesif account](https://www.moesif.com)

### Spring MVC (Java Config)

```java

import com.moesif.servlet.MoesifFilter;

import javax.servlet.Filter;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;


public class MyWebInitializer extends
		AbstractAnnotationConfigDispatcherServletInitializer {

	@Override
	protected Filter[] getServletFilters() {
		return new Filter[]{new MoesifFilter("Your Moesif Application Id")};
	}
}

```

### Spring MVC (XML Config)

In Spring MVC + XML configuration, you can register the filters via web.xml

In `web.xml` file:

```xml

  <filter>
    <filter-name>MoesifFilter</filter-name>
    <filter-class>com.moesif.servlet.MoesifFilter</filter-class>
    <init-param>
      <param-name>application-id</param-name>
      <param-value>Your Moesif Application Id</param-value>
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

### Jersey Servlet

There are multiple ways to run Jersey, as a Java Servlet or embedded with a Java NIO framework like Grizzly. This subsection focuses on running Jersey as a Servlet.

Edit the web.xml file to add your Moesif Application Id that you obtained from your Moesif Account.

```xml
  <filter>
    <filter-name>MoesifFilter</filter-name>
    <filter-class>com.moesif.servlet.MoesifFilter</filter-class>
    <init-param>
      <param-name>application-id</param-name>
      <param-value>Your Moesif Application Id</param-value>
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

#### Running the Jersey Servlet example

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
	vim jersey-servlet-example/src/main/webapp/WEB-INF/web.xml
	```

3. Run jersey-servlet-example

	```sh
	cd jersey-servlet-example
	mvn clean install
	java -jar target/dependency/webapp-runner.jar target/*.war
	```

4. Go to `http://localhost:8080/api/demo` or the port that Tomcat is running on.

In your Moesif Account, you should see event logged and monitored.

Shut it down manually with Ctrl-C.

### Spark Servlet

There are multiple ways to run Spark, as a Java Servlet or embedded with a server like Jetty. This subsection focuses on running Spark as a Servlet.

Edit the web.xml file to add your Moesif Application Id that you obtained from your Moesif Account.

```xml
  <filter>
    <filter-name>MoesifFilter</filter-name>
    <filter-class>com.moesif.servlet.MoesifFilter</filter-class>
    <init-param>
      <param-name>application-id</param-name>
      <param-value>Your Moesif Application Id</param-value>
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

In order to run this example you will need to have Java 8+ and Maven installed.

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

3. Run spark-servlet-example

	```sh
	cd spark-servlet-example
	mvn clean install
	java -jar target/dependency/webapp-runner.jar target/*.war
	```

4. Go to `http://localhost:8080/api/demo` or the port that Tomcat is running on.

In your Moesif Account, you should see event logged and monitored.

Shut it down manually with Ctrl-C.

### Generic Java Servlet

Edit the web.xml file to add your Moesif Application Id that you obtained from your Moesif Account.

```xml
  <filter>
    <filter-name>MoesifFilter</filter-name>
    <filter-class>com.moesif.servlet.MoesifFilter</filter-class>
    <init-param>
      <param-name>application-id</param-name>
      <param-value>Your Moesif Application Id</param-value>
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
	mvn clean install
	java -jar target/dependency/webapp-runner.jar target/*.war
	```

4. Go to `http://localhost:8080/api/demo` or the port that Tomcat is running on.

In your Moesif Account, you should see event logged and monitored.

Shut it down manually with Ctrl-C.

## Configuration options

To configure the filter, extend the `MoesifConfigurationAdapter` class to override a few config params or implement the entire
`MoesifConfiguration` interface.
Both will achieve similar results.

### Parameters
Override the following parameters, if needed.


Name | Required | Type & Default Value | Description |
--------- | -------- | -----------|-----------------|
|batchSize | False | Type: `number` Default `100` | BatchSize of API events that will trigger flushing of queue and sending the data to Moesif. |
|batchMaxTime| False | Type: `number in seconds` Default `2`. | This is the maximum wait time (approximately) before triggering flushing of the queue and sending to Moesif.|
|queueSize | False | Type: `number` Default `1000000` | Maximum queue capacity to hold events in memory. |
|retry | False | Type: `number` Default: `0` | Number of time to retry if fails to post to Moesif. If set, must be a number between 0 to 3. |
|updateConfigTime | False | Type: `number` Default: `300` | This is the maximum wait time (approximately) to pull the latest app config and update the cache.|

### Interface methods
Override following methods, if needed.

### 1. `public boolean skip(HttpServletRequest request, HttpServletResponse response)`
Return `true` if you want to skip logging a
request to Moesif i.e. to skip boring requests like health probes.

```java
  @Override
  public boolean skip(HttpServletRequest request, HttpServletResponse response) {
    // Skip logging health probes
    return request.getRequestURI().contains("health/probe");
  }
```

### 2. `public Object getMetadata(HttpServletRequest request, HttpServletResponse response)`
Return a Java Object that allows you to add custom metadata to the event like instanceId or traceId.
The metadata must be a simple Java object that can be converted to JSON.

```java
public Object getMetadata(HttpRequest request, ClientHttpResponse response) {
  Map<String, Object> customMetadata = new HashMap<String, Object>();
  customMetadata.put("service_name", System.getProperty("app_name"));
  return customMetadata;
}
```

### 3. `public String identifyUser(HttpServletRequest request, HttpServletResponse response)`
Highly recommended. Returns a userId as a String. 
This enables Moesif to attribute API requests to individual users so you can understand who calling your API. 
This can be used simultaneously with `identifyCompany` to track both individual customers and the companies that they are a part of.

```java
  @Override
  public String identifyUser(HttpServletRequest request, HttpServletResponse response) {
    if (request.getUserPrincipal() == null) {
        return null;
    }
    return request.getUserPrincipal().getName();
  }
```

### 4. `public String identifyCompany(HttpServletRequest request, HttpServletResponse response)`
Returns a companyId as a String. 
If your business is B2B, this enables Moesif to attribute API requests to specific companies or organizations so you can understand which accounts are calling your API. This can be used simultaneously with identifyUser to track both individual customers and the companies their a part of.

```java
  @Override
  public String identifyCompany(HttpServletRequest request, HttpServletResponse response) {
    return "12345";
  }
```

### 5. `public String getSessionToken(HttpServletRequest request, HttpServletResponse response)`

Moesif automatically detects the end user's session token or API key, but you can manually define the token for finer control.

```java
  @Override
  public String getSessionToken(HttpServletRequest request, HttpServletResponse response) {
    return request.getHeader("Authorization");
  }
```

A second example if want to use the session id
```java
  @Override
  public String getSessionToken(HttpServletRequest request, HttpServletResponse response) {
    return request.getRequestedSessionId();
  }
```
### 6. `public String getApiVersion(HttpServletRequest request, HttpServletResponse response)`
Returns a String to tag requests with a specific version of your API.

```java
  @Override
  public String getApiVersion(HttpServletRequest request, HttpServletResponse response) {
    return request.getHeader("X-Api-Version");
  }
```

### 7. `public EventModel maskContent(EventModel eventModel)`
If you want to remove any sensitive data in the HTTP headers or body before sending to Moesif, you can do so with `maskContent`


## Building moesif-servlet locally
If you are contributing to moesif-servlet, you can build it locally and install in local Maven Repo:

```sh
cd moesif-servlet
mvn clean install
```
**The below methods to update user and company are accessible via the Moesif Java API lib which Moesif Play Filter already imports as a dependency.**

## Update a Single User

Create or update a user profile in Moesif.
The metadata field can be any customer demographic or other info you want to store.
Only the `userId` field is required.
This method is a convenient helper that calls the Moesif API lib.
For details, visit the [Java API Reference](https://www.moesif.com/docs/api?java#update-a-user).

```java
MoesifFilter filter = new MoesifFilter("Your Moesif Application Id", new MoesifConfiguration());

// Campaign object is optional, but useful if you want to track ROI of acquisition channels
// See https://www.moesif.com/docs/api#users for campaign schema
CampaignModel campaign = new CampaignBuilder()
        .utmSource("google")
        .utmCampaign("cpc")
        .utmMedium("adwords")
        .utmTerm("api+tooling")
        .utmContent("landing")
        .build();

// Only userId is required
// metadata can be any custom object
UserModel user = new UserBuilder()
    .userId("12345")
    .companyId("67890") // If set, associate user with a company object
    .campaign(campaign)
    .metadata(APIHelper.deserialize("{" +
        "\"email\": \"johndoe@acmeinc.com\"," +
        "\"first_name\": \"John\"," +
        "\"last_name\": \"Doe\"," +
        "\"title\": \"Software Engineer\"," +
        "\"sales_info\": {" +
            "\"stage\": \"Customer\"," +
            "\"lifetime_value\": 24000," +
            "\"account_owner\": \"mary@contoso.com\"" +
          "}" +
        "}"))
    .build();

filter.updateUser(user);
```

## Update Users in Batch

Similar to UpdateUser, but used to update a list of users in one batch. 
Only the `userId` field is required.
This method is a convenient helper that calls the Moesif API lib.
For details, visit the [Java API Reference](https://www.moesif.com/docs/api?java#update-users-in-batch).

You can update users _synchronously_ or _asynchronously_ on a background thread. Unless you require synchronous behavior, we recommend the async versions.

```java
MoesifFilter filter = new MoesifFilter("Your Moesif Application Id", new MoesifConfiguration());

List<UserModel> users = new ArrayList<UserModel>();

UserModel userA = new UserBuilder()
        .userId("12345")
        .companyId("67890")
        .campaign(campaign)
        .metadata(APIHelper.deserialize("{" +
            "\"email\": \"johndoe@acmeinc.com\"," +
            "\"first_name\": \"John\"," +
            "\"last_name\": \"Doe\"," +
            "\"title\": \"Software Engineer\"," +
            "\"sales_info\": {" +
                "\"stage\": \"Customer\"," +
                "\"lifetime_value\": 24000," +
                "\"account_owner\": \"mary@contoso.com\"" +
              "}" +
            "}"))
        .build();
users.add(userA);

UserModel userB = new UserBuilder()
        .userId("54321")
        .companyId("67890")
        .campaign(campaign)
        .metadata(APIHelper.deserialize("{" +
            "\"email\": \"johndoe@acmeinc.com\"," +
            "\"first_name\": \"John\"," +
            "\"last_name\": \"Doe\"," +
            "\"title\": \"Software Engineer\"," +
            "\"sales_info\": {" +
                "\"stage\": \"Customer\"," +
                "\"lifetime_value\": 24000," +
                "\"account_owner\": \"mary@contoso.com\"" +
              "}" +
            "}"))
        .build();
users.add(userB);

filter.updateUsersBatch(users, callBack);
```

## Update a Single Company

Create or update a company profile in Moesif.
The metadata field can be any company demographic or other info you want to store.
Only the `company_id` field is required.
This method is a convenient helper that calls the Moesif API lib.
For details, visit the [Java API Reference](https://www.moesif.com/docs/api?java#update-a-company).

```java
MoesifFilter filter = new MoesifFilter("Your Moesif Application Id", new MoesifConfiguration());

// Campaign object is optional, but useful if you want to track ROI of acquisition channels
// See https://www.moesif.com/docs/api#update-a-company for campaign schema
CampaignModel campaign = new CampaignBuilder()
        .utmSource("google")
        .utmCampaign("cpc")
        .utmMedium("adwords")
        .utmTerm("api+tooling")
        .utmContent("landing")
        .build();

// Only companyId is required
// metadata can be any custom object
CompanyModel company = new CompanyBuilder()
    .companyId("67890")
    .companyDomain("acmeinc.com") // If set, Moesif will enrich your profiles with publicly available info 
    .campaign(campaign) 
    .metadata(APIHelper.deserialize("{" +
        "\"org_name\": \"Acme, Inc\"," +
        "\"plan_name\": \"Free\"," +
        "\"deal_stage\": \"Lead\"," +
        "\"mrr\": 24000," +
        "\"demographics\": {" +
            "\"alexa_ranking\": 500000," +
            "\"employee_count\": 47" +
          "}" +
        "}"))
    .build();

filter.updateCompany(company);
```

## Update Companies in Batch

Similar to updateCompany, but used to update a list of companies in one batch. 
Only the `company_id` field is required.
This method is a convenient helper that calls the Moesif API lib.
For details, visit the [Java API Reference](https://www.moesif.com/docs/api?java#update-companies-in-batch).


You can update users _synchronously_ or _asynchronously_ on a background thread. Unless you require synchronous behavior, we recommend the async versions.

```java
MoesifFilter filter = new MoesifFilter("Your Moesif Application Id", new MoesifConfiguration());

// Campaign object is optional, but useful if you want to track ROI of acquisition channels
// See https://www.moesif.com/docs/api#update-a-company for campaign schema
CampaignModel campaign = new CampaignBuilder()
        .utmSource("google")
        .utmCampaign("cpc")
        .utmMedium("adwords")
        .utmTerm("api+tooling")
        .utmContent("landing")
        .build();

// Only companyId is required
// metadata can be any custom object
CompanyModel company = new CompanyBuilder()
    .companyId("67890")
    .companyDomain("acmeinc.com") // If set, Moesif will enrich your profiles with publicly available info 
    .campaign(campaign) 
    .metadata(APIHelper.deserialize("{" +
        "\"org_name\": \"Acme, Inc\"," +
        "\"plan_name\": \"Free\"," +
        "\"deal_stage\": \"Lead\"," +
        "\"mrr\": 24000," +
        "\"demographics\": {" +
            "\"alexa_ranking\": 500000," +
            "\"employee_count\": 47" +
          "}" +
        "}"))
    .build();

filter.updateCompaniesBatch(companies);
```

## Troubleshooting

### How to print debug logs

If you need to print debugs logs, you can set the debug switch when initializing the MoesifFilter object.

```java
MoesifFilter filter = new MoesifFilter("Your Moesif Application Id", debug)
```

If you are using XML configuration, you can set the debug switch like below:

```xml
    <filter-name>MoesifFilter</filter-name>
    <filter-class>com.moesif.servlet.MoesifFilter</filter-class>
    <init-param>
      <param-name>application-id</param-name>
      <param-value>Your Moesif Application Id</param-value>
    </init-param>
    <init-param>
      <param-name>debug</param-name>
      <param-value>true</param-value>
    </init-param>
  </filter>
```

## How to test

1. Manually clone the git repo
2. Invoke `mvn clean install -U -Dgpg.skip` if you haven't done so.
3. Add your own application id to 'src/test/java/com/moesif/servlet/MoesifServletTests.java'. You can find your Moesif Application Id from [_Moesif Dashboard_](https://www.moesif.com/) -> _Top Right Menu_ -> _Installation_
4. From terminal/cmd navigate to the root directory of the moesif-servlet.
5. Invoke `mvn -Dtest=MoesifServletTests test` to run the tests.

## Other integrations

To view more documentation on integration options, please visit __[the Integration Options Documentation](https://www.moesif.com/docs/getting-started/integration-options/).__

[ico-built-for]: https://img.shields.io/badge/built%20for-servlet-blue.svg
[ico-version]: https://img.shields.io/maven-central/v/com.moesif.servlet/moesif-servlet
[ico-license]: https://img.shields.io/badge/License-Apache%202.0-green.svg
[ico-source]: https://img.shields.io/github/last-commit/moesif/moesif-servlet.svg?style=social

[link-built-for]: https://en.wikipedia.org/wiki/Java_servlet
[link-package]: https://search.maven.org/artifact/com.moesif.servlet/moesif-servlet
[link-license]: https://raw.githubusercontent.com/Moesif/moesif-servlet/master/LICENSE
[link-source]: https://github.com/moesif/moesif-servlet
