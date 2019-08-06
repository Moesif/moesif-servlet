#### Spring Boot example

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


5. Go to `http://localhost:8080/api` or the port that Spring Boot is running on.