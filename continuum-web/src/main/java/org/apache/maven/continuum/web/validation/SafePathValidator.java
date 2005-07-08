package org.apache.maven.continuum.web.validation;

import java.util.List;

import org.apache.maven.scm.manager.ScmManager;

import org.codehaus.plexus.formica.FormicaException;
import org.codehaus.plexus.formica.validation.AbstractValidator;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.FileUtils;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class SafePathValidator
    extends AbstractValidator
{
    public static final String FILE_SEPARATOR = "file.separator";

    public boolean validate( String path )
        throws FormicaException
    {
        // ----------------------------------------------------------------------
        // Normalize what we have to start ...
        // ----------------------------------------------------------------------

        String normalizedPath = FileUtils.normalize( path );

        if ( normalizedPath.startsWith( System.getProperty( FILE_SEPARATOR ) ) )
        {
            return false;
        }

        if ( normalizedPath.startsWith( ".." ) )
        {
            return false;
        }

        return true;
    }
}
