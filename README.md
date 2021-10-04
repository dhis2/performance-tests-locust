# performance-tests

DHIS2 performance tests using locust.io

## Requirements

Running locust locally will require the following instalations:

1. docker engine
2. docker-compose

## Getting started

1. Build performance-tests project by executing the following command: `mvn -s settings.xml compile`
   Please note, that repository uses GitHub packages and the following environment variables should be configured on
   your machine:

- GITHUB_USERNAME - your GitHub username.
- GITHUB_TOKEN - personal access
  token [created on GitHub.](https://help.github.com/en/github/authenticating-to-github/creating-a-personal-access-token-for-the-command-line)

To configure the environment variables, run:   
`export GITHUB_USERNAME=$yourUsername`  
`export GITHUB_TOKEN=$yourToken`

2. Locust performance tests expect the DHIS2 instance to be available on `http://localhost:8080/dhis`. This can be
   changed by changing the value of `target.base_uri` in [locust.properties](src/main/resources/locust.properties)
   file. DHIS2 does not have to run on the localhost. A remote instance can be used as well.

   For more configuration options, see [configuration section](#test-configuration)

3. Start locust master. From the root directory execute the following commands:

```
$ docker pull dhis2/locustio:latest
$ docker-compose up
```
Note: you can also run locust without docker. [Read more](#Running-tests-without-docker)

4. Run [main() method](src/main/java/org/hisp/dhis/Main.java).
    - You can run it directly via your IDE -> open `Main` class and click on a green arrow next to the `main()` method
    - or by using the following maven command: `mvn clean compile exec:java`

5. Visit localhost:8089

6. Enter user count and hatch rate and start swarming.


### Running tests without Docker

#### Requirements
1. Python 3
2. Locust 1.2+
3. Working DHIS2 server

#### Running
1. Install Locust (will upgrade if already exists): `pip3 install locust --upgrade`
2. Start Locust master node: `locust -f locust-master.py --master --master-bind-host 127.0.0.1 --master-bind-port 5557 --web-host=127.0.0.1`
3. Start the DHIS2 server now if you have not already
3. Make sure `locust.properties` are pointing to your local DHIS2 server
4. Compile and run this project from the command line: `mvn clean compile exec:java` (you can also start it from IntelliJ via Main.class file)
5. Open your browser and go to `http://localhost:8089` enter you desired number of workers and spawn rate, point `Host` to: `127.0.0.1` "Locust master"
6. Watch the tests and listen to your machine heats up


## Test configuration

[locust.properties](src/main/resources/locust.properties) is based on the performance test database, but should work with SL database as well.

| Key | Default value | Description |
| :---: | :--:| ------------  |
| locust.master.port | 5557 | |
| locust.master.host | 127.0.0.1 | Location of the locust master |
| target.base_uri |  `http://localhost:8080/dhis` | URL of DHIS2 instance | 
| user.admin.username | admin | Super user used to populate the cache and run tests |
| user.admin.password | district | |
| cache.reuse_cache | true | Indicates if the local cache should be reused in next test execution|
| cache.users.pool.size | 40 | Indicates how many DHIS2 users should be stored in the cache. Note that users should exist in DHIS2 DB.  |
| cache.users.use_admin_user | false | Indicates if the admin user should be used when running the tests. If there are no users matching identifier configured in `cache.users.identifier`, only admin user will be used |
| cache.users.identifier | uio | Identifier used to look up users. Users will be filtered by `displayName` property |
| cache.users.password | Test1212? | Password of users loaded in the cache | 
| locust.min_wait | 20000 | Indicates how long should locust thread wait between the tasks. The value will be a random number of ms between min_wait and max_wait values. |
| locust.max_wait | 30000 |  | 
| cache.users.ou_level | 5 | Used in filtering users to populate the cache | 
| tracker.importer.async | true | Only applicable to the new tracker importer. Used as a query param in tests interacting with /tracker endpoint. |

## Required database setup 
Tests will generate data based on the database configuration, but the following assumptions are made due to limitations in data randomizer: 
- *Programs assignment*: programs should be assigned to org units that your users have access to. For example, if you only use one user (admin) that has capture access to root OU, tests won't get the whole orgUnit hierarchy, so the data will be registered to root OU.
- *User pool (`cache.users` )*
   - all users should have same password and matching usernames (configurable)
   - all users should have access to all metadata. There are no sharing checks performed in tests. 
   - all users should have capture access to programs
   - Required user authorities: `F_VIEW_EVENT_ANALYTICS`, `F_DATAVALUE_ADD`, `F_TRACKER_IMPORTER_EXPERIMENTAL` (if NTI category is included)
   
   



