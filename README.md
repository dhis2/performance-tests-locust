# performance-tests
DHIS2 performance tests using locust.io 

## Requirements
Running locust locally will require following instalations: 
1. docker engine
2. docker-compose

## Getting started

1. Build performance-tests project by executing the following command: `mvn -s settings.xml compile`
Please note, that repository uses GitHub packages and the following environment variables should be configured on your machine: 

- GITHUB_USERNAME - your GitHub username. 
- GITHUB_TOKEN - personal access token [created on GitHub.](https://help.github.com/en/github/authenticating-to-github/creating-a-personal-access-token-for-the-command-line)

To configure the environment variables, run:   
`export GITHUB_USERNAME=$yourUsername`  
`export GITHUB_TOKEN=$yourToken`
	
2. By default the Locust performance tests expect the DHIS2 instance, against which 
   they are run, to be available on `http://localhost:8080/dhis`. This can be changed 
   by changing the value of `target.baseuri` in `src/main/resources/locust.properties` 
   file.
   
   DHIS2 does not have to run on the localhost. A remote instance can be used as well
   
3. From the root directory of the `performance-test-locust` project execute `docker-compose up` command. If this command ends up in some error for example
`Could not find any locustfile` then use this command `docker pull dhis2/locustio:latest` to download docker image and re-run `docker-compose up`.

4. Run `Main.main()` method in `performance-test-locust` project. 
    - You can run it directly via your IDE -> open `Main` class and click on a green arrow next to the `main()` method 
    - or by using the following command: `mvn clean compile exec:java`
    
5. Visit localhost:8089

6. Enter users count and hatch rate and start swarming


## Running tests locally (without Docker!)

## Requirements
1. Python 3
2. Locust 1.2+
3. Working DHIS2 server

## Running
1. Install Locust (will upgrade if already exists): `pip3 install locust --upgrade`
2. Start Locust master node: `locust -f locust-master.py --master --master-bind-host 127.0.0.1 --master-bind-port 5557 --web-host=127.0.0.1`
3. Start the DHIS2 server now if you have not already
3. Make sure `locust.properties` are pointing to your local DHIS2 server
4. Compile and run this project from the command line: `mvn clean compile exec:java` (you can also start it from IntelliJ via Main.class file)
5. Open your browser and go to `http://localhost:8089` enter you desired number of workers and spawn rate, point `Host` to: `127.0.0.1` "Locust master"
6. Watch the tests and listen to your machine heats up