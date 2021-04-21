package org.hisp.dhis.tests;

import org.hisp.dhis.tasks.DhisAbstractTask;

import java.util.List;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public interface PerformanceTest
{
    List<DhisAbstractTask> getTasks();
}
