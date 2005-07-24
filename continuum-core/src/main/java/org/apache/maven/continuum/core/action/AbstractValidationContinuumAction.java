package org.apache.maven.continuum.core.action;

import org.apache.maven.continuum.ContinuumException;
import org.codehaus.plexus.util.StringUtils;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public abstract class AbstractValidationContinuumAction
    extends AbstractContinuumAction
{
    protected void assertStringNotEmpty( String value, String fieldName )
        throws ContinuumException
    {
        if ( StringUtils.isEmpty( value ) )
        {
            throw new ContinuumException( "The " + fieldName + " has to be set." );
        }
    }
}
