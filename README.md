# Moesif Servlet SDK

 [![Built For][ico-built-for]][link-built-for]
 [![Latest Version][ico-version]][link-package]
 [![Software License][ico-license]][link-license]
 [![Source Code][ico-source]][link-source]

## Introduction

moesif-servlet is a Java SDK for capturing API traffic and sending to [Moesif](https://www.moesif.com) for analysis.

The SDK is implemented as a Java EE [Servlet Filter](https://tomcat.apache.org/tomcat-5.5-doc/servletapi/javax/servlet/Filter.html)
without importing large framework specific dependencies.
Many frameworks are built on top of the Servlet API such as Spring, Apache Struts, Jersey, etc.

If you're using a web framework that is built on the
[Servlet API](https://tomcat.apache.org/tomcat-5.5-doc/servletapi/javax/servlet/Servlet.html)
such as Spring Boot, Spring MVC, Jersey, and Apache Struts, then you can enable this SDK with minimal configuration.

Different web frameworks have different way of configuring
filters. Please take a look at the framework specific examples or reach out to us for any questions.
Refer to the framework specific documentation for adding or configuring servlet filters.

For more info, visit [Moesif's Developer Docs](https://www.moesif.com/docs)

## How to install

#### Maven users

Add the Moesif dependency to your project's pom.xml file:

```xml
<!-- Include jcenter repository if you don't already have it. -->
<repositories>
    <repository>
        <id>jcenter</id>
        <url>https://jcenter.bintray.com/</url>
    </repository>
</repositories>
    
<dependency>
    <groupId>com.moesif.servlet</groupId>
    <artifactId>moesif-servlet</artifactId>
    <version>1.5.5</version>
</dependency>
```

#### Gradle users

Add the Moesif dependency to your project's build.gradle file:

```gradle
// Include jcenter repository if you don't already have it.
repositories {
    jcenter()
}
 
dependencies {   
    compile 'com.moesif.servlet:moesif-servlet:1.5.5'
}
```

#### Others

The jars are available from a public [Bintray Jcenter](https://bintray.com/moesif/maven/moesif-servlet) repository.


## How to use

### Spring Boot

In your Spring configuration file, install the Moesif Filter object.

```java

import com.moesif.servlet.MoesifFilter;

import javax.servlet.Filter;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.context.annotation.*;
import org.springframework.http.converter.*;

@Configuration
public class MyConfig extends WebMvcConfigurerAdapter {

  @Bean
  public Filter moesifFilter() {
    return new MoesifFilter("your application id");
  }
}
```

To customize the filter, pass in a object that implements `MoesifConfiguration` such
as `MoesifConfigurationAdapter`
For details regarding `MoesifConfiguration`, see the [configuration options](#configuration-options).

```java
@Configuration
public class MyConfig extends WebMvcConfigurerAdapter {

  MoesifConfiguration config = new MoesifConfigurationAdapter() {
    @Override
    public String getSessionToken(HttpServletRequest request, HttpServletResponse response) {
      return request.getHeader("Authorization");
    }
  };

  @Bean
  public Filter moesifFilter() {
    return new MoesifFilter("your application id", config);
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
	vim spring-example/src/main/java/com/moesif/servlet/spring/MyConfig.java
	```

3. Compile the example

	```sh
	cd spring-example
	mvn clean install
	```

4. Run spring-example (from the spring-example dir)

	```sh
	java -jar target/moesif-spring-example.jar
	```

	Alternatively:

	```sh
	mvn  spring-boot:run
	```


5. Go to `http://localhost:8080/greeting` or the port that Spring Boot is running on.

### Spring MVC (Java Config)

```java

import com.moesif.servlet.MoesifFilter;

import javax.servlet.Filter;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;


public class MyWebInitializer extends
		AbstractAnnotationConfigDispatcherServletInitializer {

	@Override
	protected Filter[] getServletFilters() {
		return new Filter[]{new MoesifFilter("your application id")};
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
You may have to override `onStartup()` to pass in the MoesifConfiguration object.

#### Running the Spring MVC example

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

### Jersey Servlet

There are multiple ways to run Jersey, as a Java Servlet or embedded with a Java NIO framework like Grizzly. This subsection focuses on running Jersey as a Servlet.

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
	mvn tomcat7:run
	```

4. Go to `http://localhost:3099/api/demo` or the port that Tomcat is running on.

In your Moesif Account, you should see event logged and monitored.

Shut it down manually with Ctrl-C.

### Spark Servlet

There are multiple ways to run Spark, as a Java Servlet or embedded with a server like Jetty. This subsection focuses on running Spark as a Servlet.

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

3. Run spark-servlet-example

	```sh
	cd spark-servlet-example
	mvn tomcat7:run
	```

4. Go to `http://localhost:3099/api/demo` or the port that Tomcat is running on.

In your Moesif Account, you should see event logged and monitored.

Shut it down manually with Ctrl-C.

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

## Configuration options

To configure the filter, extend the `MoesifConfigurationAdapter` class to override a few config params or implement the entire
`MoesifConfiguration` interface.
Both will achieve similar results.


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

### 2. `public EventModel maskContent(EventModel eventModel)`
If you want to remove any sensitive data in the HTTP headers or body before sending to Moesif, you can do so with `maskContent`

### 3. `public String identifyUser(HttpServletRequest request, HttpServletResponse response)`
Highly recommended. Even though Moesif automatically detects the end userId if possible, setting this configuration
ensures the highest accuracy with user attribution.

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
You can set this configuration to add company Id to the event.

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

A second example if want to use servlet sessions
```java
  @Override
  public String getSessionToken(HttpServletRequest request, HttpServletResponse response) {
    return request.getRequestedSessionId();
  }
```

### 6. `public String getTags(HttpServletRequest request, HttpServletResponse response)`
You can add any additional tags as needed
to the event.

### 7. `public String getApiVersion(HttpServletRequest request, HttpServletResponse response)`
You can optionally add an API version
to the event.

```java
  @Override
  public String getApiVersion(HttpServletRequest request, HttpServletResponse response) {
    return request.getHeader("X-Api-Version");
  }
```

## Building moesif-servlet locally
If you are contributing to moesif-servlet, you can build it locally and install in local Maven Repo:

```sh
cd moesif-servlet
mvn clean install
```

## UpdateUser method

A method is attached to the servlet object to update the users profile or metadata.
The metadata field can be any custom data you want to set on the user. The `user_id` field is required.

```java
MoesifFilter filter = new MoesifFilter("Your Application Id", new MoesifConfiguration());

UserModel user = new UserBuilder()
    .userId("javaapiuser")
    .modifiedTime(new Date())
    .ipAddress("29.80.250.240")
    .sessionToken("di3hd982h3fubv3yfd94egf")
    .userAgentString("Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36")
    .metadata(APIHelper.deserialize("{" +
        "\"email\": \"johndoe@acmeinc.com\"," +
        "\"string_field\": \"value_1\"," +
        "\"number_field\": 0," +
        "\"object_field\": {" +
        "\"field_1\": \"value_1\"," +
        "\"field_2\": \"value_2\"" +
        "}" +
        "}"))
    .build();
filter.updateUser(user);
```

## UpdateUsersBatch method

A method is attached to the servlet object to update the users profile or metadata in batch.
The metadata field can be any custom data you want to set on the user. The `user_id` field is required.

```java
MoesifFilter filter = new MoesifFilter("Your Application Id", new MoesifConfiguration());
List<UserModel> users = new ArrayList<UserModel>();

HashMap<String, Object> metadata = new HashMap<String, Object>();
metadata = APIHelper.deserialize("{" +
    "\"email\": \"johndoe@acmeinc.com\"," +
    "\"string_field\": \"value_1\"," +
    "\"number_field\": 0," +
    "\"object_field\": {" +
    "\"field_1\": \"value_1\"," +
    "\"field_2\": \"value_2\"" +
    "}" +
    "}");

UserModel userA = new UserBuilder()
    .userId("javaapiuser")
    .modifiedTime(new Date())
    .ipAddress("29.80.250.240")
    .sessionToken("di3hd982h3fubv3yfd94egf")
    .userAgentString("Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36")
    .metadata(metadata)
    .build();

UserModel userB = new UserBuilder()
		.userId("javaapiuser1")
    .modifiedTime(new Date())
    .ipAddress("29.80.250.240")
    .sessionToken("di3hd982h3fubv3yfd94egf")
    .userAgentString("Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36")
    .metadata(metadata)
    .build();

users.add(userA);
users.add(userB);

filter.updateUsersBatch(users);
```

## UpdateCompany method

A method is attached to the servlet object to update the company profile or metadata.
The metadata field can be any custom data you want to set on the company. The `company_id` field is required.

```java
MoesifFilter filter = new MoesifFilter("Your Application Id", new MoesifConfiguration());

CompanyModel company = new CompanyBuilder()
				.companyId("javaapicompany")
				.companyDomain("acmeinc.com")
				.metadata(APIHelper.deserialize("{" +
						"\"email\": \"johndoe@acmeinc.com\"," +
						"\"string_field\": \"value_1\"," +
						"\"number_field\": 0," +
						"\"object_field\": {" +
						"\"field_1\": \"value_1\"," +
						"\"field_2\": \"value_2\"" +
						"}" +
						"}"))
				.build();
filter.updateCompany(company);
```

## UpdateCompaniesBatch method

A method is attached to the servlet object to update the companies profile or metadata in batch.
The metadata field can be any custom data you want to set on the company. The `company_id` field is required.

```java
MoesifFilter filter = new MoesifFilter("Your Application Id", new MoesifConfiguration());
List<CompanyModel> companies = new ArrayList<CompanyModel>();

HashMap<String, Object> metadata = new HashMap<String, Object>();
metadata = APIHelper.deserialize("{" +
    "\"email\": \"johndoe@acmeinc.com\"," +
    "\"string_field\": \"value_1\"," +
    "\"number_field\": 0," +
    "\"object_field\": {" +
    "\"field_1\": \"value_1\"," +
    "\"field_2\": \"value_2\"" +
    "}" +
    "}");

  CompanyModel companyA = new CompanyBuilder()
  		.companyId("javaapicompany")
  		.companyDomain("nowhere.com")
  		.metadata(metadata)
  		.build();

  CompanyModel companyB = new CompanyBuilder()
  		.companyId("javaapicompany1")
  		.companyDomain("nowhere.com")
  		.metadata(metadata)
  		.build();

  companies.add(companyA);
  companies.add(companyB);

  filter.updateCompaniesBatch(companies);
```

## How to test

1. Manually clone the git repo
2. Invoke `mvn clean install -U -Dgpg.skip` if you haven't done so.
3. Add your own application id to 'src/test/java/com/moesif/servlet/MoesifServletTests.java'. You can find your Application Id from [_Moesif Dashboard_](https://www.moesif.com/) -> _Top Right Menu_ -> _Installation_
4. From terminal/cmd navigate to the root directory of the moesif-servlet.
5. Invoke `mvn -Dtest=MoesifServletTests test` to run the tests.

## Other integrations

To view more more documentation on integration options, please visit __[the Integration Options Documentation](https://www.moesif.com/docs/getting-started/integration-options/).__

[ico-built-for]: https://img.shields.io/badge/built%20for-servlet-blue.svg
[ico-version]: https://api.bintray.com/packages/moesif/maven/moesif-servlet/images/download.svg
[ico-license]: https://img.shields.io/badge/License-Apache%202.0-green.svg
[ico-source]: https://img.shields.io/github/last-commit/moesif/moesif-servlet.svg?style=social

[link-built-for]: https://en.wikipedia.org/wiki/Java_servlet
[link-package]: https://bintray.com/moesif/maven/moesif-servlet/_latestVersion
[link-license]: https://raw.githubusercontent.com/Moesif/moesif-servlet/master/LICENSE
[link-source]: https://github.com/moesif/moesif-servlet
