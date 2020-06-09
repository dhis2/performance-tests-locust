package org.hisp.dhis.locust;

import org.aeonbits.owner.Config;

@Config.Sources( { "system:properties", "system:env", "classpath:config.properties" } )
public interface LocustConfig
    extends
    Config
{
    @Key( "locust.master.port" )
    int locustMasterPort();

    @Key( "locust.master.host" )
    String locustMasterHost();

    @Key( "target.baseuri" )
    String targetUri();

    @Key( "analytics.api.version" )
    int analyticsApiVersion();
}
