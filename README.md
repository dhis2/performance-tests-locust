# performance-tests
DHIS2 performance tests using locust.io 

## Requirements
Running locust locally will require the following instalations: 
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
	
2. Locust performance tests expect the DHIS2 instance to be available on `http://localhost:8080/dhis`. This can be changed 
   by changing the value of `target.base_uri` in [locust.properties](src/main/resources/locust.properties) 
   file. DHIS2 does not have to run on the localhost. A remote instance can be used as well.
   
   For more configuration options, see [configuration section](#test-configuration)
   
3. From the root directory of the `performance-test-locust` project execute `docker-compose up` command.

4. Run [main() method](src/main/java/org/hisp/dhis/Main.java).
    - You can run it directly via your IDE -> open `Main` class and click on a green arrow next to the `main()` method 
    - or by using the following maven command: `mvn clean compile exec:java`
    
5. Visit localhost:8089

6. Enter user count and hatch rate and start swarming. 	

## Test configuration

[locust.properties](src/main/resources/locust.properties) is based on the performance test database, but should work with SL database as well. 

| Key | Default value | Description |
| --- | :----------:| ---- : |
| locust.master.port | 5557 | |
| locust.master.host | 127.0.0.1 | Location of the locust master
| target.base_uri |  `http://localhost:8080/dhis` | URL of DHIS2 instance | 
| user.admin.username | admin | Super user used to populate the cache and run tests |
| user.admin.password | district | |
| cache.reuse_cache | true | Indicates if the local cache should be reused in next test execution|
| cache.users.pool.size | 40 | Indicates how many DHIS2 users should be stored in the cache. Note that users should exist in DHIS2 DB.  |
| cache.users.use_admin_user | false | Indicates if the admin user should be used when running the tests. If there are no users matching identifier configured in `cache.users.identifier`, admin user will be used anyway.
| cache.users.identifier | uio | Identifier used to look up users. Users will be filtered by `displayName` property |
| cache.users.password | Test1212? | Password of users loaded in the cache | 
| locust.min_wait | 20000 | Indicates how long should locust thread wait between the tasks. The value will be a random number of ms between min_wait and max_wait values. |
| locust.max_wait | 30000 |  | 




