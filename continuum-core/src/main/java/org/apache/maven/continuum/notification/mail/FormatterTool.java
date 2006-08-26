package org.apache.maven.continuum.notification.mail;

/*
 * Copyright 2004-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.maven.continuum.project.ContinuumProjectState;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class FormatterTool
{
    private String timestampFormatString;

    private ThreadLocal timestampFormat = new ThreadLocal();

    public FormatterTool( String timestampFormatString )
    {
        this.timestampFormatString = timestampFormatString;
    }

    // TODO: Add i18n
    public String formatProjectState( int state )
    {
        if ( state == ContinuumProjectState.NEW || state == ContinuumProjectState.CHECKEDOUT )
        {
            return "New";
        }
        else if ( state == ContinuumProjectState.OK )
        {
            return "Ok";
        }
        else if ( state == ContinuumProjectState.FAILED )
        {
            return "Failed";
        }
        else if ( state == ContinuumProjectState.ERROR )
        {
            return "Error";
        }
        else if ( state == ContinuumProjectState.BUILDING )
        {
            return "Building";
        }
        else
        {
            return "Unknown project state '" + state + "'";
        }
    }

    public String formatTrigger( int trigger )
    {
        if ( trigger == ContinuumProjectState.TRIGGER_SCHEDULED )
        {
            // TODO: fix this
            return "Schedule";
        }
        else if ( trigger == ContinuumProjectState.TRIGGER_FORCED )
        {
            return "Forced";
        }
        else
        {
            return "Unknown build trigger: '" + trigger + "'";
        }
    }

    public String formatTimestamp( long timestamp )
    {
        if (timestamp <= 0) {
            return null;
        }
        return getSimpleDateFormat( timestampFormat, timestampFormatString ).format( new Date( timestamp ) );
    }

    public String formatInterval( long start, long end )
    {
        long diff = end - start;

        long interval = diff / 1000L;

        long hours = interval / 3600L;

        interval -= hours * 3600;

        long minutes = interval / 60;

        interval -= minutes * 60;

        long seconds = interval;

        if ( hours > 0 )
        {
            return Long.toString( hours ) + "h " + Long.toString( minutes ) + "m " + Long.toString( seconds ) + "s";
        }

        if ( minutes > 0 )
        {
            return Long.toString( minutes ) + "m " + Long.toString( seconds ) + "s";
        }

        return Long.toString( seconds ) + "s";
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private SimpleDateFormat getSimpleDateFormat( ThreadLocal threadLocal, String format )
    {
        SimpleDateFormat dateFormat = (SimpleDateFormat) threadLocal.get();

        if ( dateFormat == null )
        {
            dateFormat = new SimpleDateFormat( format );

            threadLocal.set( dateFormat );
        }

        return dateFormat;
    }

    public String trim(String str) {
        if (str == null) {
            return "";
        }
        return str.trim();
    }
}
