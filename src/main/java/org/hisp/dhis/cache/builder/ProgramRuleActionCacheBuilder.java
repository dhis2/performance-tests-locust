package org.hisp.dhis.cache.builder;

import org.hisp.dhis.actions.RestApiActions;
import org.hisp.dhis.cache.EntitiesCache;
import org.hisp.dhis.cache.ProgramRuleAction;
import org.hisp.dhis.response.dto.ApiResponse;

import java.util.List;
import java.util.logging.Logger;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class ProgramRuleActionCacheBuilder
    implements CacheBuilder<ProgramRuleAction>
{
    private Logger logger = Logger.getLogger( this.getClass().getName() );

    @Override
    public void load( EntitiesCache cache )
    {
        List<ProgramRuleAction> programRuleActions = get();
        cache.setProgramRuleActions( programRuleActions );

        logger.info( "Program rule actions loaded in cache. Size: " + programRuleActions.size() );
    }

    @Override
    public List<ProgramRuleAction> get()
    {
        ApiResponse response = getPayload(
            "/api/programRuleActions?filter=programRuleActionType:eq:ASSIGN&fields=id,dataElement[id],trackedEntityAttribute[id]&paging=false" );

        return response.extractList( "programRuleActions", ProgramRuleAction.class );
    }

    private ApiResponse getPayload( String url )
    {
        return new RestApiActions( "" ).get( url );
    }

}
