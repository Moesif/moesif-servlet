# Moesif Spring Request SDK

## Introduction

moesif-springrequest is a Java SDK for capturing outgoing API traffic and sending to [Moesif](https://www.moesif.com) for analysis.

The SDK is implemented as a Spring Request Interceptor.

For more info, visit [Moesif's Developer Docs](https://www.moesif.com/docs)

## How to install

#### Maven users

Add the Moesif dependency to your project's pom.xml file:

```xml
<dependency>
    <groupId>com.moesif.springrequest</groupId>
    <artifactId>moesif-springrequest</artifactId>
    <version>1.1.2</version>
</dependency>
```

#### Gradle users

Add the Moesif dependency to your project's build.gradle file:

```gradle
dependencies {   
    compile 'com.moesif.springrequest:moesif-springrequest:1.1.2'
}
```

## How to use

### 1. Inject The Moesif Interceptor
```java
RestTemplate template = new RestTemplate();

final List<ClientHttpRequestInterceptor> interceptors = new ArrayList<ClientHttpRequestInterceptor>();

interceptors.add(new MoesifSpringRequestInterceptor("{{props.appId}}"));

template.setInterceptors(interceptors);
```

### 2. Make HTTP requests using the same `RestTemplate`
```java
template.exchange(
  "https://jsonplaceholder.typicode.com/posts",
  HttpMethod.POST,
  "{\"id\": \"1\"}",
  String.class
);
```

## Configuration options

To configure the filter, extend the `MoesifRequestConfiguration` class to override a few config params.


### 1. `public boolean skip(HttpRequest request, ClientHttpResponse response)`
Return `true` if you want to skip logging a
request to Moesif i.e. to skip boring requests like health probes.

```java
  @Override
  public boolean skip(HttpRequest request, ClientHttpResponse response) {
    // Skip logging health probes
    return request.getURI().toString().contains("health/probe");
  }
```

### 2. `public EventModel maskContent(EventModel eventModel)`
If you want to remove any sensitive data in the HTTP headers or body before sending to Moesif, you can do so with `maskContent`

### 3. `public String identifyUser(HttpRequest request, ClientHttpResponse response)`
Highly recommended. Even though Moesif automatically detects the end userId if possible, setting this configuration
ensures the highest accuracy with user attribution.

```java
  @Override
  public String identifyUser(HttpRequest request, ClientHttpResponse response) {
    return request.getHeaders().getFirst("user");
  }
```

### 4. `public String identifyCompany(HttpRequest request, ClientHttpResponse response)`
You can set this configuration to add company Id to the event.

```java
  @Override
  public String identifyCompany(HttpRequest request, ClientHttpResponse response) {
    return "12345";
  }
```

### 5. `public String getSessionToken(HttpRequest request, ClientHttpResponse response)`

Moesif automatically detects the end user's session token or API key, but you can manually define the token for finer control.

```java
  @Override
  public String getSessionToken(HttpRequest request, ClientHttpResponse response) {
    return request.getHeaders().getFirst("Authorization");
  }
```

### 6. `public String getApiVersion(HttpRequest request, ClientHttpResponse response)`
You can optionally add an API version
to the event.

```java
  @Override
  public String getApiVersion(HttpRequest request, ClientHttpResponse response) {
    return request.getHeaders().getFirst("X-Api-Version");
  }
```

## Building moesif-springrequest locally
If you are contributing to moesif-springrequest, you can build it locally and install in local Maven Repo:

```sh
cd moesif-springrequest
mvn clean install
```

## Enable Debug Messsages

```java
RequestConfig requestConfig = new RequestConfig();

requestConfig.debug = true;

interceptors.add(new MoesifSpringRequestInterceptor(
  "Enter your Moesif AppId here",
  requestConfig
));
```

## Disable Logging Request and Response Body

Optional, Default `true`. Set `logBody` flag to `false` to remove logging request and response body to Moesif.

```java
RequestConfig requestConfig = new RequestConfig();
    
// Set logBody flag to false to remove logging request and response body to Moesif
requestConfig.logBody = true;

interceptors.add(new MoesifSpringRequestInterceptor(
  "Enter your Moesif AppId here",
  requestConfig
));
```

## Other integrations

To view more documentation on integration options, please visit __[the Integration Options Documentation](https://www.moesif.com/docs/getting-started/integration-options/).__

[ico-built-for]: https://img.shields.io/badge/built%20for-servlet-blue.svg
[ico-version]: https://api.bintray.com/packages/moesif/maven/moesif-springrequest/images/download.svg
[ico-license]: https://img.shields.io/badge/License-Apache%202.0-green.svg
[ico-source]: https://img.shields.io/github/last-commit/moesif/moesif-servlet.svg?style=social

[link-built-for]: https://en.wikipedia.org/wiki/Spring_Framework
[link-package]: https://bintray.com/moesif/maven/moesif-springframework/_latestVersion
[link-license]: https://raw.githubusercontent.com/Moesif/moesif-servlet/master/LICENSE
[link-source]: https://github.com/moesif/moesif-servlet
