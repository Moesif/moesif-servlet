#### Spring Boot example deployed as a servlet webapp

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
	vim spring-boot-servlet-example/src/main/java/com/moesif/servlet/spring/MyConfig.java
	```

3. Compile the example

	```sh
	cd spring-boot-servlet-example
	mvn clean install
	```

4. Run spring-boot-servlet-example (from the spring-boot-servlet-example dir)

	```sh
        java -jar target/dependency/webapp-runner.jar target/*.war
	```


5. Using Postman or CURL, make a few API calls to `http://localhost:8080/api` or the port that Spring Boot is running on.
   
6. Verify the API calls are logged to your [Moesif account](https://www.moesif.com)