# Moesif Servlet SDK

[![](https://jitpack.io/v/com.moesif/moesif-servlet.svg)](https://jitpack.io/#com.moesif/moesif-servlet)

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

For more info, visit [Moesif's Developer Docs](https://www.moesif.com/docs) or the [JavaDoc](https://jitpack.io/com/moesif/moesif-servlet/servlet-filter/-SNAPSHOT/javadoc/)

## How to install

```xml
<!-- Step 1. Add the JitPack repository to your build file -->

<repositories>
	<repository>
	    <id>jitpack.io</id>
	    <url>https://jitpack.io</url>
	</repository>
</repositories>


<!-- Step 2. Add the dependency -->

<dependency>
    <groupId>com.moesif.moesif-servlet</groupId>
    <artifactId>servlet-filter</artifactId>
    <version>1.0.4</version>
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

#### How to Compile the Spring Boot Example

```bash
git clone https://github.com/Moesif/moesif-servlet
cd moesif-servlet
mvn clean install
```
#### How to run the Spring Boot Example

```bash
java -jar spring-example/target/moesif-spring-example.jar
```

or

```bash
mvn spring-boot:run
```

### Spring MVC (Java Config))

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

### General Java HTTP Servlet

In order to run this example you will need to have Java 7+ and Maven installed.

Check that your maven version is 3.0.x or above:

```sh
mvn -v
```

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

#### How to Compile the Spring Boot Example

```bash
git clone https://github.com/Moesif/moesif-servlet
cd moesif-servlet
mvn clean install
```

#### How to run the Servlet Example


```bash
mvn tomcat7:run -pl servlet-example
```

Then, go to `http://localhost:3099/Demo1`.

In your Moesif Account, you should see event logged and monitored.

Shut it down manually with Ctrl-C.

### Configuration options

To configure the filter, extend the `MoesifConfigurationAdapter` class to override a few config params or implement the entire
`MoesifConfiguration` interface.
Both will achieve similar results.


#### 1. `public boolean skip(HttpServletRequest request, HttpServletResponse response)`
Return `true` if you want to skip logging a
request to Moesif i.e. to skip boring requests like health probes.

```java
  @Override
  public boolean skip(HttpServletRequest request, HttpServletResponse response) {
    // Skip logging health probes
    return request.getRequestURI().contains("health/probe");
  }
```

#### 2. `public EventModel maskContent(EventModel eventModel)`
If you want to remove any sensitive data in the HTTP headers or body before sending to Moesif, you can do so with `maskContent`

#### 3. `public String identifyUser(HttpServletRequest request, HttpServletResponse response)`
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

#### 4. `public String getSessionToken(HttpServletRequest request, HttpServletResponse response)` 

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

#### 5. `public String getTags(HttpServletRequest request, HttpServletResponse response)`
You can add any additional tags as needed
to the event.

#### 6. `public String getApiVersion(HttpServletRequest request, HttpServletResponse response)`
You can optionally add an API version
to the event.

```java
  @Override
  public String getApiVersion(HttpServletRequest request, HttpServletResponse response) {
    return request.getHeader("X-Api-Version");
  }
```

## Other integrations

To view more more documentation on integration options, please visit __[the Integration Options Documentation](https://www.moesif.com/docs/getting-started/integration-options/).__
