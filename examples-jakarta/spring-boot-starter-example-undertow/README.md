#### Spring Boot 3.2 Jakarta example using the Spring Boot Starter Parent with Undertow

In order to run this example you will need to have Java 19+ and Maven installed.

Before starting: 
1. Check that your maven version is 3.2.x or above
2. JDK 19 or above
3. Undertow 

```sh
mvn -v
```

1. Clone the repository, and go to the example folder

   ```sh
   git clone https://github.com/Moesif/moesif-servlet
   cd moesif-servlet/examples-jakarta/spring-boot-starter-example-undertow
   ```

2. Update MyConfig to use your own Moesif ApplicationId
   (Register for an account on [moesif.com](https://www.moesif.com))

   ```sh
   vim src/main/java/com/moesif/moesifservlet/example/undertow/MyConfig.java
   ```

3. Compile the example

   ```sh
   mvn clean install
   ```

4. Run the sample
   ```sh
   mvn spring-boot:run
   ```


5. Using Postman or CURL, make a few API calls to `http://localhost:8080/` or the port that Spring Boot is running on.

6. Verify the API calls are logged to your [Moesif account](https://www.moesif.com)