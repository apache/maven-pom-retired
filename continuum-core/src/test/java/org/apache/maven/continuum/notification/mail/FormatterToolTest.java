package org.apache.maven.continuum.notification.mail;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

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
