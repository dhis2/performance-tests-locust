package org.hisp.dhis.conf;

import java.util.List;

@org.aeonbits.owner.Config.LoadPolicy( org.aeonbits.owner.Config.LoadType.MERGE )
@org.aeonbits.owner.Config.Sources( { "system:properties",
    "system:env",
    "file:/conf/locust.properties",
    "classpath:locust.properties" } )
public interface TestConfig
    extends
    org.aeonbits.owner.Config
{
    @Key( "locust.master.port" )
    int locustMasterPort();

    @Key( "locust.master.host" )
    String locustMasterHost();

    @Key( "locust.log_full_errors" )
    Boolean locustLogFullErrors();

    @Key( "locust.min_wait" )
    int locustMinWaitBetweenTasks();

    @Key( "locust.max_wait" )
    int locustMaxWaitBetweenTasks();

    @Key( "target.base_uri" )
    String targetUri();

    @Key( "user.admin.username" )
    String adminUsername();

    @Key( "user.admin.password" )
    String adminPassword();

    @Key( "cache.users.pool.size" )
    int cacheUserPoolSize();

    @Key( "cache.users.use_admin_user" )
    boolean useDefaultUser();

    @Key( "cache.users.identifier" )
    String cacheUsersIdentifier();

    @Key( "cache.users.password" )
    String cacheUsersPassword();

    @Key( "cache.reuse_cache" )
    boolean reuseCache();

    @Key( "cache.users.ou_levels" )
    List<Integer> cacheUsersOuLevels();

    @Key( "tracker.importer.async" )
    boolean useAsyncTrackerImporter();

    @Key( "logging.debug" )
    boolean debug();

    @Key( "locust.random.seed" )
    long locustRandomSeed();
}
