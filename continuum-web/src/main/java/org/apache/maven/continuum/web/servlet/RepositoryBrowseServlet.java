package org.apache.maven.continuum.web.servlet;

/*
 * Copyright 2006 The Apache Software Foundation.
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

import org.apache.maven.continuum.configuration.ConfigurationService;
import org.apache.maven.continuum.utils.PlexusContainerManager;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringUtils;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 * @version $Id$
 */
public class RepositoryBrowseServlet
    extends HttpServlet
{
    private ConfigurationService configuration;

    public void init( ServletConfig servletConfig )
        throws ServletException
    {
        super.init( servletConfig );

        PlexusContainer container = PlexusContainerManager.getInstance().getContainer();

        try
        {
            configuration = (ConfigurationService) container.lookup( ConfigurationService.ROLE );
        }
        catch ( ComponentLookupException e )
        {
            throw new ServletException( "Unable to get configuration service", e );
        }
    }

    protected void doGet( HttpServletRequest req, HttpServletResponse res )
        throws ServletException, IOException
    {
        File f = getFile( req );

        if ( f.exists() )
        {
            doDownload( res, f );
        }
        else
        {
            res.sendError( HttpServletResponse.SC_NOT_FOUND );
        }
    }

    private File getFile( HttpServletRequest req )
    {
        String path = "";

        if ( StringUtils.isNotEmpty( req.getPathInfo() ) )
        {
            path = req.getPathInfo().substring( 1 );
        }

        //Clean url, so url like ../../../../a_file and /path/to_file like /etc/passwd won't be allow
        String fileName = cleanUrl( path );

        if ( fileName.indexOf( ".." ) >= 0 || fileName.startsWith( "/" ) )
        {
            throw new IllegalArgumentException( "file " + fileName + " isn't allowed." );
        }

        return new File( configuration.getDeploymentRepositoryDirectory(), fileName );
    }

    protected long getLastModified( HttpServletRequest req )
    {
        File f = getFile( req );

        long mod;
        if ( f.exists() )
        {
            mod = f.lastModified();
        }
        else
        {
            mod = super.getLastModified( req );
        }
        return mod;
    }

    public String getServletInfo()
    {
        return this.getClass().getName() + " by Continuum Team";
    }

    private void doDownload( HttpServletResponse response, File file )
        throws IOException
    {
        if ( !file.isFile() )
        {
            response.sendError( HttpServletResponse.SC_FORBIDDEN );
        }
        else
        {
            ServletOutputStream output = response.getOutputStream();

            ServletContext context = getServletConfig().getServletContext();

            String mimetype = context.getMimeType( file.getName() );

            if ( mimetype == null )
            {
                mimetype = "application/octet-stream";
            }

            response.setContentType( mimetype );

            if ( !mimetype.startsWith( "text/" ) )
            {
                response.setHeader( "Content-Disposition", "attachement; filename=\"" + file.getName() + "\"" );
            }

            IOUtil.copy( new FileInputStream( file ), output );
        }
    }

    private String cleanUrl( String url )
    {
        if ( url == null )
        {
            throw new NullPointerException( "The url cannot be null." );
        }

        String pathSeparator = "";

        int indexOfDoubleDot = -1;

        // Clean Unix path
        if ( url.indexOf( "../" ) > 1 )
        {
            pathSeparator = "/";

            indexOfDoubleDot = url.indexOf( "../" );
        }

        // Clean windows path
        if ( url.indexOf( "..\\" ) > 1 )
        {
            pathSeparator = "\\";

            indexOfDoubleDot = url.indexOf( "..\\" );
        }

        String newUrl = url;
        if ( indexOfDoubleDot > 1 )
        {
            int startOfTextToRemove = url.substring( 0, indexOfDoubleDot - 1 ).lastIndexOf( pathSeparator );

            String beginUrl = "";
            if ( startOfTextToRemove >= 0 )
            {
                beginUrl = url.substring( 0, startOfTextToRemove );
            }

            String endUrl = url.substring( indexOfDoubleDot + 3 );

            newUrl = beginUrl + pathSeparator + endUrl;

            // Check if we have other double dot
            if ( newUrl.indexOf( "../" ) > 1 || newUrl.indexOf( "..\\" ) > 1 )
            {
                newUrl = cleanUrl( newUrl );
            }
        }

        return newUrl;
    }
}
