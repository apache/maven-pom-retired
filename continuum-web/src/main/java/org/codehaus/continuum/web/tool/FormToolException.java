package org.codehaus.continuum.web.tool;

/**
 * @author <a href="mailto:jason@maven.org">Jason van Zyl</a>
 * @version $Id:$
 */
public class FormToolException
    extends Exception
{
    public FormToolException( String message )
    {
        super( message );
    }

    public FormToolException( Throwable cause )
    {
        super( cause );
    }

    public FormToolException( String message, Throwable cause )
    {
        super( message, cause );
    }
}
