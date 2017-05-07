# Moesif Servlet SDK

 [ ![Download](https://api.bintray.com/packages/moesif/maven/moesif-servlet/images/download.svg) ](https://bintray.com/moesif/maven/moesif-servlet/_latestVersion)

## Introduction

moesif-servlet is a Java SDK for capturing API traffic and sending to [Moesif](https://www.moesif.com) for analysis.

The SDK is implemented as a Java EE [Servlet Filter](https://tomcat.apache.org/tomcat-5.5-doc/servletapi/javax/servlet/Filter.html)
without importing large framework specific dependencies.
Many frameworks are built on top of the Servlet API such as Spring, Apache Struts, Jersey, etc.

If your using a web framework that is built on the
[Servlet API](https://tomcat.apache.org/tomcat-5.5-doc/servletapi/javax/servlet/Servlet.html)
such as Spring Boot, Spring MVC, Jersey, and Apache Struts, then you can enable this SDK with minimal configuration.

Different web frameworks have different way of configuring
filters. Please take a look at the framework specific examples or reach out to us for any questions.
Refer to the framework specific documentation for adding or configuring servlet filters.

For more info, visit [Moesif's Developer Docs](https://www.moesif.com/docs)

## How to install

```xml
<!-- Step 1. Add the Bintray repository to your build file -->

<repositories>
    <repository>
      <id>bintray-moesif-maven</id>
      <name>bintray</name>
      <url>http://dl.bintray.com/moesif/maven</url>
    </repository>
</repositories>


<!-- Step 2. Add the dependency -->

<dependency>
    <groupId>com.moesif.servlet</groupId>
    <artifactId>moesif-servlet</artifactId>
    <version>1.1.0</version>
</dependency>
```

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
	```

2. Update MyConfig to use your own Moesif ApplicationId
(Register for an account on [moesif.com](https://www.moesif.com))

	```sh
	vim moesif-servlet/spring-example/src/main/java/com/moesif/servlet/spring/MyConfig.java
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
      <param-value>true</param-value>
    </init-param>
  </filter>
  <filter-mapping>
    <filter-name>MoesifFilter</filter-name>
    <url-pattern>/*</url-pattern>
  </filter-mapping>


```
You may have to override `onStartup()` to pass in the MoesifConfiguration object.

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
	```

2. Update web.xml to use your own Moesif ApplicationId
(Register for an account on [moesif.com](https://www.moesif.com))

	```sh
	vim moesif-servlet/servlet-example/src/main/webapp/WEB-INF/web.xml
	```

3. Run servlet-example

	```sh
	cd servlet-example
	mvn tomcat7:run
	```

4. Go to `http://localhost:3099/demo` or the port that Tomcat is running on.

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

### 4. `public String getSessionToken(HttpServletRequest request, HttpServletResponse response)`

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

### 5. `public String getTags(HttpServletRequest request, HttpServletResponse response)`
You can add any additional tags as needed
to the event.

### 6. `public String getApiVersion(HttpServletRequest request, HttpServletResponse response)`
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


## Other integrations

To view more more documentation on integration options, please visit __[the Integration Options Documentation](https://www.moesif.com/docs/getting-started/integration-options/).__
