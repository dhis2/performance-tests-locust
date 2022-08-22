package org.hisp.dhis.random;

import org.apache.commons.collections.set.ListOrderedSet;
import org.hisp.dhis.cache.DataElement;
import org.hisp.dhis.cache.EntitiesCache;
import org.hisp.dhis.dxf2.events.event.DataValue;
import org.hisp.dhis.utils.DataRandomizer;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class EventDataValueRandomizer
    implements DhisEntityRandomizer<ListOrderedSet>
{

    private DataValue withRandomValue( DataElement dataElement )
    {
        DataValue dataValue = new DataValue();
        dataValue.setDataElement( dataElement.getUid() );
        dataValue.setProvidedElsewhere( false );
        String val;
        if ( !CollectionUtils.isEmpty( dataElement.getOptionSet() ) )
        {
            val = DataRandomizer.randomElementFromList( dataElement.getOptionSet() );
        }
        else
        {
            val = new DataValueRandomizer().rndValueFrom( dataElement.getValueType() );
        }

        dataValue.setValue( val );
        return dataValue;
    }

    @Override
    public ListOrderedSet create( EntitiesCache cache, RandomizerContext randomizerContext )
    {
        List<DataElement> dataElements = randomizerContext.getProgramStage().getProgramStageDataElements();

        ListOrderedSet dataValues = new ListOrderedSet();
        if ( randomizerContext.isSkipGenerationWhenAssignedByProgramRules() )
        {
            dataElements.removeIf( DataElement::isGeneratedByProgramRules );
        }

        dataElements
            .forEach( p -> dataValues.add( withRandomValue( p ) ) );

        return dataValues;
    }

}
