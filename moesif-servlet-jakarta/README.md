# Moesif Servlet Jakarta SDK

 [![Built For][ico-built-for]][link-built-for]
 [![Latest Version][ico-version]][link-package]
 [![Software License][ico-license]][link-license]
 [![Source Code][ico-source]][link-source]

## Introduction

`moesif-servlet-jakarta` is a Jakarta Servlet Filter that logs _incoming_ API calls and sends to [Moesif](https://www.moesif.com) for API analytics and monitoring.

The SDK is implemented as a [Jakarta Servlet Filter](https://tomcat.apache.org/tomcat-10.0-doc/servletapi/jakarta/servlet/Filter.html)
without importing framework specific dependencies. Any framework built on Java [Jakarta Servlet API](https://tomcat.apache.org/tomcat-10.0-doc/servletapi/jakarta/servlet/Servlet.html) such as Spring, Struts, Jersey, etc can use this SDK with minimal configuration.

[Source Code on GitHub](https://github.com/moesif/moesif-servlet)

## How to install

#### Maven users

Add the Moesif dependency to your project's pom.xml file:

```xml
<dependency>
    <groupId>com.moesif.servlet</groupId>
    <artifactId>moesif-servlet-jakarta</artifactId>
    <version>2.0.3</version>
</dependency>
```

#### Gradle users

Add the Moesif dependency to your project's build.gradle file:

```gradle
dependencies {   
    compile 'com.moesif.servlet:moesif-servlet-jakarta:2.0.2'
}
```

## How to use

Different Java web frameworks have different way of configuring filters. 
Go to your specific framework's instructions below:

- [Spring Boot Starter Example Jakarta](#spring-boot-starter-example-jakarta)


Your Moesif Application Id can be found in the [_Moesif Portal_](https://www.moesif.com/).
After signing up for a Moesif account, your Moesif Application Id will be displayed during the onboarding steps. 

You can always find your Moesif Application Id at any time by logging 
into the [_Moesif Portal_](https://www.moesif.com/), click on the top right menu,
and then clicking _Installation_.

### Spring Boot Starter Example Jakarta

In your Spring configuration file, install the Moesif Filter object.

```java

import com.moesif.servlet.MoesifFilter;

import jakarta.servlet.Filter;
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

In order to run this example you will need to have Java 17+ and Maven installed.

Before starting, check that your maven version is 3.0.x or above:

```sh
mvn -v
```

1. Clone the repository

	```sh
	git clone https://github.com/Moesif/moesif-servlet
  cd moesif-servlet/examples/spring-boot-starter-example-jakarta
	```

2. Update MyConfig to use your own Moesif ApplicationId
(Register for an account on [moesif.com](https://www.moesif.com))

	```sh
	vim src/main/java/com/moesif/servlet/spring/MyConfig.java
	```

3. Compile the example

	```sh
	mvn clean install
	```

4. Run

	```sh
	mvn  spring-boot:run
	```

5. Using Postman or CURL, make a few API calls to `http://localhost:8080/api` or the port that Spring Boot is running on.
   
6. Verify the API calls are logged to your [Moesif account](https://www.moesif.com)


## Building moesif-servlet locally
If you are contributing to moesif-servlet, you can build it locally and install in local Maven Repo:

```sh
cd moesif-servlet-jakarta
mvn clean install
```

## How to test

1. Manually clone the git repo
2. Invoke `mvn clean install -U -Dgpg.skip` if you haven't done so.
3. Add your own application id to 'src/test/java/com/moesif/servlet/MoesifServletTests.java'. You can find your Moesif Application Id from [_Moesif Dashboard_](https://www.moesif.com/) -> _Top Right Menu_ -> _Installation_
4. From terminal/cmd navigate to the root directory of the moesif-servlet-jakarta.
5. Invoke `mvn -Dtest=MoesifServletTests test` to run the tests.

[ico-built-for]: https://img.shields.io/badge/built%20for-servlet-blue.svg
[ico-version]: https://img.shields.io/maven-central/v/com.moesif.servlet/moesif-servlet-jakarta
[ico-license]: https://img.shields.io/badge/License-Apache%202.0-green.svg
[ico-source]: https://img.shields.io/github/last-commit/moesif/moesif-servlet.svg?style=social

[link-built-for]: https://en.wikipedia.org/wiki/Jakarta_Servlet
[link-package]: https://search.maven.org/artifact/com.moesif.servlet/moesif-servlet-jakarta
[link-license]: https://raw.githubusercontent.com/Moesif/moesif-servlet/master/LICENSE
[link-source]: https://github.com/moesif/moesif-servlet
