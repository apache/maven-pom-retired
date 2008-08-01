/**
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

package org.apache.maven.mercury.spi.http.server;

import org.mortbay.util.IO;
import org.mortbay.util.URIUtil;

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;


/**
 * DefaultBatchFilter
 * <p/>
 * Handles the atomic upload (using PUT messages) of a batch of files. "Atomic" means
 * that either all file uploads succeed or none do. This transactionality can only be
 * guaranteed when using the mercury client, as a "commit/discard" message
 * is sent from the client side to indicate how to terminate the mercury operation. If
 * a commit is received, then all files that form part of the batch - indicated by a
 * batch id in the PUT headers - are moved from a staging location to the final
 * location. If a discard is received, then all files forming part of the mercury will
 * be deleted. If the client side is not the jetty batcher, then the server side
 * cannot know when the batch has ended, and therefore will immediately copy files
 * to their final locations during the PUT.
 */
public class StagingBatchFilter extends BatchFilter
{
    private String _stagingDirURI;

    public void init( FilterConfig config )
        throws ServletException
    {
        super.init( config );

        //allow tmp dir location to be configured
        String t = config.getInitParameter( "stagingDirURI" );
        if ( t != null )
        {
            _stagingDirURI = t;
        }
        else
        {
            //fall back to WEB-INF/lib
            File f = new File( _context.getRealPath( "/" ) );
            File w = new File( f, "WEB-INF" );
            File l = new File( w, "lib" );
            _stagingDirURI = l.toURI().toString();
        }
    }


    /**
     * Put the file to a staging area before doing move to final location
     * on a commit.
     *
     * @see BatchFilter#putFile(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.String, java.lang.String)
     */
    public void putFile( HttpServletRequest request,
                         HttpServletResponse response,
                         String pathInContext,
                         String batchId )
        throws Exception
    {
        String stagedResource = URIUtil.addPaths( _stagingDirURI,
            batchId ); //put the file into staging dir under the batchid
        stagedResource = URIUtil.addPaths( stagedResource, pathInContext );
        File stagedFile = null;

        try
        {
            stagedFile = new File( new URI( stagedResource ) );
            File parent = stagedFile.getParentFile();
            parent.mkdirs();

            int toRead = request.getContentLength();
            InputStream in = request.getInputStream();
            OutputStream out = new FileOutputStream( stagedFile, false );
            if ( toRead >= 0 )
            {
                IO.copy( in, out, toRead );
            }
            else
            {
                IO.copy( in, out );
            }
            out.close();
        }
        catch ( Exception e )
        {
            try
            {
                if ( stagedFile.exists() )
                {
                    stagedFile.delete();
                }
                throw e;
            }
            catch ( Exception ex )
            {
                _context.log( ex.toString(), ex );
            }
        }
    }


    /**
     * Do the move of all files in mercury to a final location
     *
     * @see BatchFilter#commitFiles(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.String)
     */
    public boolean commitFiles( HttpServletRequest request, HttpServletResponse response, Batch batch )
        throws Exception
    {
        if ( batch == null )
        {
            return true; //nothing to do
        }

        boolean ok = true;
        String stagedResource = URIUtil.addPaths( _stagingDirURI, batch.getBatchId() );
        File batchDir = new File( new URI( stagedResource ) );
        File[] files = batchDir.listFiles();
        for ( int i = 0; files != null && i < files.length; i++ )
        {
            String name = files[i].getName();
            File dest = new File( new URI( URIUtil.addPaths( _baseURI, name ) ) );
            if ( !files[i].renameTo( dest ) )
            {
                ok = false;
            }
        }
        if ( ok )
        {
            ok = batchDir.delete();
        }
        return ok;
    }


    /**
     * Delete all files in the mercury from the staging area.
     *
     * @see BatchFilter#discardFiles(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.String)
     */
    public boolean discardFiles( HttpServletRequest request, HttpServletResponse response, Batch batch )
        throws Exception
    {
        if ( batch == null )
        {
            return true; //nothing to do
        }

        String stagedResource = URIUtil.addPaths( _stagingDirURI, batch.getBatchId() );
        File batchDir = new File( new URI( stagedResource ) );
        boolean ok = true;
        if ( !deleteFile( batchDir ) )
        {
            ok = false;
        }
        return ok;
    }


    /**
     * Recursively descend file hierarchy and delete all files.
     *
     * @param f
     * @return
     */
    private boolean deleteFile( File f )
    {

        if ( f == null )
        {
            return true;
        }
        if ( f.isFile() )
        {
            return f.delete();
        }
        else if ( f.isDirectory() )
        {
            File[] files = f.listFiles();
            boolean ok = true;
            for ( int i = 0; files != null && i < files.length; i++ )
            {
                if ( !deleteFile( files[i] ) )
                {
                    ok = false;
                }
            }

            if ( !f.delete() )
            {
                ok = false;
            }

            return ok;
        }
        else
        {
            return true;
        }
    }
}
