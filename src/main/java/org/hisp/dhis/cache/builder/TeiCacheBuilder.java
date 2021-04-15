package org.hisp.dhis.cache.builder;

import com.google.common.collect.Lists;
import org.hisp.dhis.actions.RestApiActions;
import org.hisp.dhis.cache.EntitiesCache;
import org.hisp.dhis.cache.Program;
import org.hisp.dhis.cache.Tei;
import org.hisp.dhis.response.dto.ApiResponse;
import org.hisp.dhis.utils.DataRandomizer;

import java.util.*;
import java.util.logging.Logger;

import static java.util.stream.Collectors.toList;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class TeiCacheBuilder
    implements CacheBuilder<Tei>
{
    private Logger logger = Logger.getLogger( this.getClass().getName() );

    @Override
    public void load( EntitiesCache cache )
    {
        Map<String, List<Tei>> tempMap = new HashMap<>();

        for ( Program program : cache.getTrackerPrograms() )
        {
            //List<List<String>> partitions = Lists.partition( program.getOrgUnits(), 500);
            if ( program.getOrgUnits().size() == 0 )
            {
                logger.info( String.format( "Program %s doesn't have any org units", program.getUid() ) );
                continue;
            }

            // get org units that are assigned to users in the user pool

            List<String> userOrgUnits = cache.getUsers().parallelStream()
                .flatMap( p -> p.getOrganisationUnits().stream() )
                .filter( ou -> program.getOrgUnits().contains( ou ) )
                .collect( toList() );

            userOrgUnits = userOrgUnits.stream().filter( ou -> program.getOrgUnits().contains( ou ) ).collect( toList() );

            List<String> orgUnits = DataRandomizer.randomElementsFromList( userOrgUnits, 1000 );
            List<List<String>> partitions = Lists.partition( orgUnits, 250 );

            partitions.forEach( p -> {
                final String ous = String.join( ";", p );
                List<Map> payload = getPayload(
                    "/api/trackedEntityInstances?ou=" + ous + "&pageSize=50&program=" + program.getUid() )
                    .extractList( "trackedEntityInstances" );

                // -- create a List of Tei for the current Program and OU
                List<Tei> teisFromProgram = new ArrayList<>();

                for ( Map map : payload )
                {
                    teisFromProgram.add( new Tei( (String) map.get( "trackedEntityInstance" ), program.getUid(),
                        (String) map.get( "orgUnit" ) ) );
                }

                if ( tempMap.containsKey( program.getUid() ) )
                {
                    List<Tei> teis = tempMap.get( program.getUid() );
                    teis.addAll( teisFromProgram );
                }
                else
                {
                    tempMap.put( program.getUid(), teisFromProgram );
                }
            } );
        }

        cache.setTeis( tempMap );
        logger.info( "TEIs loaded in cache. Size: " + tempMap.values().stream().mapToInt( Collection::size ).sum() );

    }

    private ApiResponse getPayload( String url )
    {
        return new RestApiActions( "" ).get( url );
    }
}
