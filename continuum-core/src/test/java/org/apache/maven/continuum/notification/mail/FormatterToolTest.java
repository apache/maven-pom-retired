/*
 * Copyright (c) 2005 Your Corporation. All Rights Reserved.
 */
package org.apache.maven.continuum.notification.mail;

import junit.framework.TestCase;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class FormatterToolTest
    extends TestCase
{
    public void testIntervalFormatting()
        throws Exception
    {
        FormatterTool tool = new FormatterTool( null );

        assertEquals( "10s", tool.formatInterval( 0, makeTime( 0, 0, 10 ) ) );

        assertEquals( "1m 0s", tool.formatInterval( 0, makeTime( 0, 1, 0 ) ) );

        assertEquals( "1m 10s", tool.formatInterval( 0, makeTime( 0, 1, 10 ) ) );

        assertEquals( "1h 0m 0s", tool.formatInterval( 0, makeTime( 1, 0, 0) ) );

        assertEquals( "1h 10m 0s", tool.formatInterval( 0, makeTime( 1, 10, 0) ) );

        assertEquals( "1h 1m 10s", tool.formatInterval( 0, makeTime( 1, 1, 10 ) ) );

        assertEquals( "8s", tool.formatInterval( 1112561981137L, 1112561990023L ) );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private long makeTime( int hours, int minutes, int seconds )
    {
        return ( hours * 3600 + minutes * 60 + seconds ) * 1000;
    }
}
