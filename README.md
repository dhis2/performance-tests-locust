# performance-tests

DHIS2 performance tests using locust.io

## Run the tests locally

There are two options to run the tests locally - with and without Docker.

### Run with Docker

**Requirements**

- [Docker](https://docs.docker.com/get-docker)
- [Docker Compose](https://docs.docker.com/compose/install)
- Working DHIS2 server

**Steps**

1. Pull the existing Docker images with `docker compose pull` or build them with `docker compose build`.
2. Make sure the DHIS2 server is running. It doesn't have to be a DHIS2 server running locally, it can be a remote one, as well.
3. Start the Locust master and worker with the following example command (you can change the `TIME`, `HATCH_RATE` and `USERS` according to your needs):
```shell
NO_WEB=true TIME=30m HATCH_RATE=10 USERS=100 TARGET=http://localhost:8080 MASTER_HOST=master docker compose up --abort-on-container-exit
```

_Note that you can also omit the `NO_WEB=true` environment variable, which will start the Locust master with a web UI, where you'll be able to configure the Time, Hatch Rate and Users for the tests._

### Run without Docker

**Requirements**

- [Python 3](https://www.python.org/downloads)
- [Locust.io](https://docs.locust.io/en/stable/installation.html)
- Working DHIS2 server

**Steps**

1. Install Locust (or upgrade if it's already installed): `pip3 install locust --upgrade`.
2. Start Locust master node: `locust -f locust-master.py --master --master-bind-host 127.0.0.1 --master-bind-port 5557 --web-host=127.0.0.1`.
3. Make sure the DHIS2 server is running. It doesn't have to be a DHIS2 server running locally, it can be a remote one, as well.
4. Make sure that `target.base_uri` in [locust.properties](src/main/resources/locust.properties) is pointing to the DHIS2 server of choice.
5. Compile and run this project from the command line: `mvn clean compile exec:java` (you can also start it from IntelliJ via the [main() method](src/main/java/org/hisp/dhis/Main.java)).
6. Open your browser and go to `http://localhost:8089` enter you desired number of workers and spawn rate, point `Host` to: `127.0.0.1` "Locust master".
7. Watch the tests and listen to your machine heating up.

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
| cache.users.ou_levels | 5 | Used to restrict users to the organisation unit levels (comma separated list). Set lowest level ou to model data entry users. Set to 0 if ou level doesn't matter. |
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
