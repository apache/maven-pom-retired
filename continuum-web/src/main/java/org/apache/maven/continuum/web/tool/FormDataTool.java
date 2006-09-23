package org.apache.maven.continuum.web.tool;

import org.codehaus.plexus.util.StringUtils;

import java.util.List;
import java.util.ArrayList;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class FormDataTool
{
    public List pulldown( String data )
    {
        List list = new ArrayList();

        String[] items = StringUtils.split( data, "," );

        for ( int i = 0; i < items.length; i++ )
        {
            String[] s = StringUtils.split( items[i], "=" );

            Item item = new Item( s[0], s[1] );

            list.add( item );
        }

        return list;
    }
}
