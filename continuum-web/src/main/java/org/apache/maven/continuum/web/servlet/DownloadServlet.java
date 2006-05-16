package org.apache.maven.continuum.web.servlet;

/*
 * Copyright 2001-2006 The Apache Software Foundation.
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
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringInputStream;
import org.codehaus.plexus.util.StringUtils;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class DownloadServlet
    extends HttpServlet
{
    private static final int BUFSIZE = 2048;

    private PlexusContainer container;

    private Logger logger;

    public void init( ServletConfig servletConfig )
        throws ServletException
    {
        super.init( servletConfig );

        container = PlexusContainerManager.getInstance().getContainer();

        logger = container.getLogger();
    }

    protected void doGet( HttpServletRequest req, HttpServletResponse res )
        throws ServletException, IOException
    {
        String fileName = req.getParameter( "file" );

        if ( StringUtils.isEmpty( fileName ) )
        {
            res.sendError( HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "You must specified the 'file' parameter." );

            return;
        }

        File f = null;

        try
        {
            f = getFile( fileName );
        }
        catch ( Exception e )
        {
            logger.error( "Can't get file " + fileName, e );
        }

        if ( f != null && f.exists() )
        {
            doDownload( req, res, f, fileName );
        }
        else
        {
            res.sendError( HttpServletResponse.SC_NOT_FOUND );
        }
    }

    protected long getLastModified( HttpServletRequest req )
    {
        String fileName = req.getParameter( "file" );

        if ( StringUtils.isNotEmpty( fileName ) )
        {
            try
            {
                File f = getFile( fileName );

                if ( f != null && f.exists() )
                {
                    return f.lastModified();
                }
            }
            catch ( Exception e )
            {
                logger.error( "Can't get file " + fileName, e );
            }
        }

        return super.getLastModified( req );
    }

    public String getServletInfo()
    {
        return this.getClass().getName() + " by Continuum Team";
    }

    private File getFile( String fileName )
        throws Exception
    {
        ConfigurationService configuration = (ConfigurationService) container.lookup( ConfigurationService.ROLE );

        //Clean url, so url like ../../../../a_file and /path/to_file like /etc/passwd won't be allow
        fileName = cleanUrl( fileName );

        if ( fileName.indexOf( ".." ) >= 0 || fileName.startsWith( "/" ) )
        {
            throw new IllegalArgumentException( "file " + fileName + " isn't allowed." );
        }

        File f = new File( configuration.getWorkingDirectory(), fileName );

        if ( f.exists() )
        {
            return f;
        }

        f = new File( configuration.getBuildOutputDirectory(), fileName );

        if ( f.exists() )
        {
            return f;
        }

        return null;
    }

    private void doDownload( HttpServletRequest req, HttpServletResponse response, File file, String fileNameParam )
        throws IOException
    {
        File currentFile = file;

        if ( currentFile.isDirectory() )
        {
            File index = new File( file, "index.html" );

            if ( index.exists() )
            {
                currentFile = index;
            }
            else
            {
                index = new File( file, "index.htm" );

                if ( index.exists() )
                {
                    currentFile = index;
                }
            }
        }

        DataInputStream input = null;

        byte[] bbuf = new byte[BUFSIZE];

        try
        {
            String fileName = currentFile.getName();

            int length;

            ServletOutputStream output = response.getOutputStream();

            ServletContext context = getServletConfig().getServletContext();

            if ( currentFile.isFile() )
            {
                String mimetype = context.getMimeType( fileName );

                mimetype = ( mimetype != null ) ? mimetype : "application/octet-stream";

                response.setContentType( mimetype );

                if ( !mimetype.startsWith( "text/" ) )
                {
                    response.setHeader( "Content-Disposition", "attachement; filename=\"" + fileName + "\"" );
                }

                input = new DataInputStream( getContent( req, response, currentFile, mimetype, fileNameParam ) );

                while ( ( ( length = input.read( bbuf ) ) != -1 ) )
                {
                    output.write( bbuf, 0, length );
                }

                output.flush();
            }
            else
            {
                response.sendError( HttpServletResponse.SC_FORBIDDEN );
            }
        }
        finally
        {
            if ( input != null )
            {
                input.close();
            }
        }
    }

    private InputStream getContent( HttpServletRequest req, HttpServletResponse response, File file, String mimetype,
                                    String fileNameParam )
        throws IOException
    {
        if ( !"text/html".equals( mimetype ) && !"text/css".equals( mimetype ) )
        {
            response.setContentLength( (int) file.length() );
            return new FileInputStream( file );
        }
        else
        {
            String content = rewriteFileContent( req, file, fileNameParam );
            response.setContentLength( content.length() );
            return new StringInputStream( content );
        }
    }

    private String rewriteFileContent( HttpServletRequest req, File file, String fileNameParam )
        throws IOException
    {
        String extension = FileUtils.getExtension( file.getName() );

        String content = FileUtils.fileRead( file );

        if ( "html".equalsIgnoreCase( extension ) || "htm".equalsIgnoreCase( extension ) )
        {
            content = rewriteSrcInHtml( req, "img", content, fileNameParam );
            content = rewriteSrcInHtml( req, "script", content, fileNameParam );
            content = rewriteHrefInHtml( req, "a", content, fileNameParam );
            content = rewriteHrefInHtml( req, "link", content, fileNameParam );
            content = rewriteImportInHtml( req, content, fileNameParam );
        }
        else if ( "css".equalsIgnoreCase( extension ) )
        {
            content = rewriteUrlInCSS( req, content, fileNameParam );
        }
        return content;
    }

    private String rewriteUrlInCSS( HttpServletRequest req, String cssContent, String fileNameParam )
    {
        int startUrl = cssContent.indexOf( "url(" );
        if ( startUrl < 0 )
        {
            return cssContent;
        }

        int endUrl = cssContent.indexOf( ")", startUrl );

        if ( endUrl < 0 )
        {
            return cssContent;
        }

        return cssContent.substring( 0, startUrl + 4 ) +
            rewriteUrl( req, cssContent.substring( startUrl + 4, endUrl ), fileNameParam ) +
            rewriteUrlInCSS( req, cssContent.substring( endUrl ), fileNameParam );
    }

    private String rewriteSrcInHtml( HttpServletRequest req, String tagName, String htmlContent, String fileNameParam )
    {
        int startImg = htmlContent.indexOf( "<" + tagName + " " );

        if ( startImg < 0 )
        {
            return htmlContent;
        }

        int endImg = htmlContent.indexOf( ">", startImg );

        int srcPos = htmlContent.indexOf( "src=\"", startImg );

        if ( endImg > srcPos && srcPos > 0 )
        {
            int endSrcPos = htmlContent.indexOf( "\"", srcPos + 5 );
            String url = htmlContent.substring( srcPos + 5, endSrcPos );

            return htmlContent.substring( 0, srcPos + 5 ) + rewriteUrl( req, url, fileNameParam ) +
                rewriteSrcInHtml( req, tagName, htmlContent.substring( endSrcPos ), fileNameParam );
        }
        else
        {
            return htmlContent.substring( 0, endImg + 1 ) +
                rewriteSrcInHtml( req, tagName, htmlContent.substring( endImg ), fileNameParam );
        }
    }

    private String rewriteHrefInHtml( HttpServletRequest req, String tagName, String htmlContent, String fileNameParam )
    {
        int startA = htmlContent.indexOf( "<" + tagName + " " );

        if ( startA < 0 )
        {
            return htmlContent;
        }

        int endA = htmlContent.indexOf( ">", startA );

        int hrefPos = htmlContent.indexOf( "href=\"", startA );

        if ( endA > hrefPos && hrefPos > 0 )
        {
            int endHrefPos = htmlContent.indexOf( "\"", hrefPos + 6 );

            String url = htmlContent.substring( hrefPos + 6, endHrefPos );

            return htmlContent.substring( 0, hrefPos + 6 ) + rewriteUrl( req, url, fileNameParam ) +
                rewriteHrefInHtml( req, tagName, htmlContent.substring( endHrefPos ), fileNameParam );
        }
        else
        {
            return htmlContent.substring( 0, endA ) +
                rewriteHrefInHtml( req, tagName, htmlContent.substring( endA ), fileNameParam );
        }
    }

    private String rewriteImportInHtml( HttpServletRequest req, String htmlContent, String fileNameParam )
    {
        int startImport = htmlContent.indexOf( "@import " );

        if ( startImport < 0 )
        {
            return htmlContent;
        }

        int endImport = htmlContent.indexOf( ";", startImport );

        if ( endImport < 0 )
        {
            return htmlContent;
        }

        int startUrl = htmlContent.indexOf( "\"", startImport + 8 );

        if ( startUrl < 0 || startUrl > endImport )
        {
            return htmlContent.substring( 0, endImport ) +
                rewriteImportInHtml( req, htmlContent.substring( endImport + 1 ), fileNameParam );
        }

        int endUrl = htmlContent.indexOf( "\"", startUrl + 1 );

        if ( endUrl < 0 || endUrl > endImport )
        {
            return htmlContent.substring( 0, endImport ) +
                rewriteImportInHtml( req, htmlContent.substring( endImport + 1 ), fileNameParam );
        }

        return htmlContent.substring( 0, startUrl + 1 ) +
            rewriteUrl( req, htmlContent.substring( startUrl + 1, endUrl ), fileNameParam ) +
            rewriteImportInHtml( req, htmlContent.substring( endUrl ), fileNameParam );
    }

    private String rewriteUrl( HttpServletRequest req, String url, String fileNameParam )
    {
        String param = StringUtils.replace( fileNameParam, "\\", "/" );

        if ( url.startsWith( "#" ) )
        {
            //anchor
            return url;
        }

        if ( url.indexOf( "://" ) > 0 )
        {
            //absolute url
            return url;
        }

        if ( url.startsWith( "/" ) )
        {
            return url;
        }

        if ( url.startsWith( "./" ) )
        {
            url = url.substring( 2 );
        }

        int lastSlash = param.lastIndexOf( "/" );

        String result = req.getRequestURI() + "?file=";

        String dirName;

        if ( lastSlash > 0 )
        {
            dirName = param.substring( 0, lastSlash );
        }
        else
        {
            dirName = param;
        }

        result += cleanUrl( dirName + "/" + url );

        return result;
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

        if ( indexOfDoubleDot > 1 )
        {
            int startOfTextToRemove = url.substring( 0, indexOfDoubleDot - 1 ).lastIndexOf( pathSeparator );

            String beginUrl = "";
            if ( startOfTextToRemove >= 0 )
            {
                beginUrl = url.substring( 0, startOfTextToRemove );
            }

            String endUrl = url.substring( indexOfDoubleDot + 3 );

            url = beginUrl + pathSeparator + endUrl;

            // Check if we have other double dot
            if ( url.indexOf( "../" ) > 1 || url.indexOf( "..\\" ) > 1 )
            {
                url = cleanUrl( url );
            }
        }

        return url;
    }
}
