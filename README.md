# performance-tests
DHIS2 performance tests using locust.io 

## Requirements
Running locust locally will require following instalations: 
1. docker engine
2. docker-compose
## Getting started

1. Build performance-tests project by executing the following command: `mvn compile`
	
2. By default the Locust performance tests expect the DHIS2 instance, against which 
   they are run, to be available on `http://localhost:8080/dhis`. This can be changed 
   by changing the value of `target.baseuri` in `src/main/resources/locust.properties` 
   file.
   
   DHIS2 does not have to run on the localhost. A remote instance can be used as well
3. From the root directory of the `performance-test-locust` project execute `docker-compose up` command.
4. Run `Main.main()` method in the `performance-test-locust` project. 
    - You can run it directly via your IDE -> open `Main` class and click on a green arrow next to the `main()` method 
    - or by using the following command: `mvn clean compile exec:java`
5. Visit localhost:8089
6. Enter users count and hatch rate and start swarming


