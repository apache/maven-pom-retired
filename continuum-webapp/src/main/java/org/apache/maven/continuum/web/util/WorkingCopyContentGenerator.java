package org.apache.maven.continuum.web.util;

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

import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.util.Iterator;
import java.util.List;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class WorkingCopyContentGenerator
    extends AbstractLogEnabled
{
    private File basedir;

    private String urlParamSeparator;

    public String generate( Object item, String baseUrl, File basedir )
    {
        this.basedir = basedir;
        if ( baseUrl.indexOf( "?" ) > 0 )
        {
            urlParamSeparator = "&";
        }
        else
        {
            urlParamSeparator = "?";
        }

        List directoryEntries = (List) item;

        StringBuffer buf = new StringBuffer();

        buf.append( "+&nbsp;<a href=\"" + baseUrl + urlParamSeparator + "userDirectory=/\">/</a><br />" );

        print( directoryEntries, "&nbsp;&nbsp;", baseUrl, buf );

        return buf.toString();
    }

    private void print( List dirs, String indent, String baseUrl, StringBuffer buf )
    {
        for ( Iterator i = dirs.iterator(); i.hasNext(); )
        {
            Object obj = i.next();

            print( obj, indent, baseUrl, buf );
        }
    }
    private void print( Object obj, String indent, String baseUrl, StringBuffer buf )
    {
        if ( obj instanceof File )
        {
            File f = (File) obj;;

            if ( !f.isDirectory() )
            {
                String fileName = f.getName();

                if ( !".cvsignore".equals( fileName ) && !"vssver.scc".equals( fileName ) && !".DS_Store".equals( fileName ) )
                {
                    String userDirectory = null;

                    if ( f.getParentFile().getAbsolutePath().equals( basedir.getAbsolutePath() ) )
                    {
                        userDirectory = "/";
                    }
                    else
                    {
                        userDirectory = f.getParentFile().getAbsolutePath().substring( basedir.getAbsolutePath().length() + 1 );
                    }

                    userDirectory = StringUtils.replace( userDirectory, "\\", "/" );

                    buf.append( indent + "&nbsp;&nbsp;&nbsp;<a href=\"" + baseUrl + urlParamSeparator + "userDirectory=" + userDirectory + "&file=" + fileName + "\">" + fileName + "</a><br />" );
                }
            }
            else
            {
                String directoryName = f.getName();

                if ( !"CVS".equals( directoryName ) && !".svn".equals( directoryName ) && !"SCCS".equals( directoryName ) )
                {
                    String userDirectory = f.getAbsolutePath().substring( basedir.getAbsolutePath().length() + 1 );

                    userDirectory = StringUtils.replace( userDirectory, "\\", "/" );

                    buf.append( indent + "+&nbsp;<a href=\"" + baseUrl + urlParamSeparator + "userDirectory=" + userDirectory + "\">" + directoryName + "</a><br />" );
                }
            }
        }
        else
        {
            print( (List) obj, indent + "&nbsp;&nbsp;", baseUrl, buf );
        }
    }
}