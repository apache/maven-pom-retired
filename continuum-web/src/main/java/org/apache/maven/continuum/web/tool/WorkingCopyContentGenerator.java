package org.apache.maven.continuum.web.tool;

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
import org.codehaus.plexus.summit.pull.RequestTool;
import org.codehaus.plexus.summit.rundata.RunData;
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
    implements RequestTool
{
    private String contextPath;

    private File basedir;

    public String generate( Object item, String baseUrl, File basedir )
    {
        this.basedir = basedir;

        List directoryEntries = (List) item;

        StringBuffer buf = new StringBuffer();

        buf.append( "+&nbsp;<a href=\"" ).append( baseUrl ).append( "?userDirectory=/\">/</a><br />" );

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
            File f = (File) obj;

            if ( !f.isDirectory() )
            {
                String fileName = f.getName();

                if ( !".cvsignore".equals( fileName ) && !"vssver.scc".equals( fileName ) &&
                    !".DS_Store".equals( fileName ) )
                {
                    String userDirectory;

                    if ( f.getParentFile().getAbsolutePath().equals( basedir.getAbsolutePath() ) )
                    {
                        userDirectory = "/";
                    }
                    else
                    {
                        userDirectory =
                            f.getParentFile().getAbsolutePath().substring( basedir.getAbsolutePath().length() + 1 );
                    }

                    userDirectory = StringUtils.replace( userDirectory, "\\", "/" );

                    buf.append( indent ).append( "&nbsp;&nbsp;&nbsp;<a target=\"blank\" href=\"" ).append(
                        getFileUrl( userDirectory, fileName ) ).append( "\">" ).append( fileName ).append(
                        "</a><br />" );
                }
            }
            else
            {
                String directoryName = f.getName();

                if ( !"CVS".equals( directoryName ) && !".svn".equals( directoryName ) &&
                    !"SCCS".equals( directoryName ) && !".bzr".equals( directoryName ) )
                {
                    String userDirectory = f.getAbsolutePath().substring( basedir.getAbsolutePath().length() + 1 );

                    userDirectory = StringUtils.replace( userDirectory, "\\", "/" );

                    buf.append( indent ).append( "+&nbsp;<a href=\"" ).append( baseUrl ).append(
                        "?userDirectory=" ).append( userDirectory ).append( "\">" ).append( directoryName ).append(
                        "</a><br />" );
                }
            }
        }
        else
        {
            print( (List) obj, indent + "&nbsp;&nbsp;", baseUrl, buf );
        }
    }

    private String getBrowseServletPath()
    {
        return contextPath + "/servlet/browse?file=";
    }

    private String getFileUrl( String directory, String fileName )
    {
        String filePath;

        if ( StringUtils.isEmpty( directory ) || "/".equals( directory ) )
        {
            filePath = basedir.getName() + "/" + fileName;
        }
        else
        {
            filePath = basedir.getName() + "/" + directory + "/" + fileName;
        }

        return getBrowseServletPath() + filePath;
    }

    public void setRunData( RunData data )
    {
        contextPath = data.getContextPath();
    }

    public void refresh()
    {
        // empty
    }
}