

# Starter Example for Servelet

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


In order to build and run the project you must execute:

```sh
mvn clean install tomcat7:run
```

Then, go to [http://localhost:3099/Demo1](http://localhost:3099/Demo1).

In your Moesif Account, you should see event logged and monitored. 


Shut it down manually with Ctrl-C.