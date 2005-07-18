package org.apache.maven.continuum.configuration;

import org.codehaus.plexus.util.xml.Xpp3Dom;

import java.io.File;

/**
 * @author <a href="mailto:jason@maven.org">Jason van Zyl</a>
 * @version $Id:$
 */
public interface ConfigurationService
{
    String ROLE = ConfigurationService.class.getName();

    void load()
        throws ConfigurationLoadingException;

    void store()
        throws ConfigurationStoringException;
}
