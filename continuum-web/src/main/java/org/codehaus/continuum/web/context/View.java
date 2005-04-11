package org.apache.maven.continuum.web.context;

import java.util.List;

/**
 * @author <a href="mailto:jason@maven.org">Jason van Zyl</a>
 * @version $Id: View.java,v 1.1 2005/04/01 00:11:34 jvanzyl Exp $
 */
public class View
{
    private String id;

    private List scalars;

    public String getId()
    {
        return id;
    }

    public List getScalars()
    {
        return scalars;
    }
}
