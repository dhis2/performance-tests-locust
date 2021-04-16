package org.hisp.dhis.tests;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
@Retention( RetentionPolicy.RUNTIME )
@Target( ElementType.TYPE )
public @interface Category
{
    CategoryType category() default CategoryType.ALL;
}

