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

import org.mortbay.jetty.HttpMethods;
import org.mortbay.servlet.PutFilter;
import org.mortbay.util.IO;
import org.mortbay.util.URIUtil;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * BatchFilter
 * <p/>
 * Base class for handling atomic uploads of batches of files.
 * Subclasses should implement their own means of making the
 * uploads atomic. The methods putFile, commitFiles, discardFiles
 * can be overridden/implemented in order to facilitate this.
 * For example, the DefaultBatchFilter subclass copies all files
 * to a staging area before moving them to their final locations
 * upon receipt of a Jetty-Batch-Commit message.
 * <p/>
 * TODO consider having a scavenger thread to remove failed or incomplete uploads?
 *
 * @see org.sonatype.mercury.server.jetty.DefaultBatchFilter
 */
public abstract class BatchFilter extends PutFilter
{
    protected ConcurrentMap<String, Batch> _batches = new ConcurrentHashMap<String, Batch>();
    protected String _batchIdHeader = "Jetty-Batch-Id";
    protected String _batchSupportedHeader = "Jetty-Batch-Supported";
    protected String _batchCommitHeader = "Jetty-Batch-Commit";
    protected String _batchDiscardHeader = "Jetty-Batch-Discard";

    /**
     * Batch
     * <p/>
     * Retains the status of a mercury. If a mercury succeeds it is removed
     * from the list. If it fails, then it is retained in the list
     * but marked as failed. If a mercury is not completed, then the
     * timestamp can be used by a timer thread to clean up.
     */
    protected class Batch
    {
        protected String _batchId;
        protected long _timestamp;
        protected boolean _ok;
        protected List<String> _files;


        public Batch( String batchId, long timestamp )
        {
            _batchId = batchId;
            _timestamp = timestamp;
            _files = new ArrayList<String>();
        }

        public String getBatchId()
        {
            return _batchId;
        }

        public void addFile( String file )
        {
            _files.add( file );
        }

        public List getFiles()
        {
            return _files;
        }

        public void failed()
        {
            _ok = false;
        }

        public boolean isOK()
        {
            return _ok;
        }

        public long getTimestamp()
        {
            return _timestamp;
        }

        public String toString()
        {
            return "BatchStatus: id=" + _batchId + " ts=" + _timestamp + " count=" + _files.size() + ", " + _ok;
        }
    }

    /**
     * Implement this method to finish the upload of the files by making them
     * available for download. When this method returns, all files forming part of
     * the mercury should be available.
     *
     * @param request
     * @param response
     * @param batchId
     * @return
     * @throws Exception
     */
    public abstract boolean commitFiles( HttpServletRequest request, HttpServletResponse response, Batch batch )
        throws Exception;

    /**
     * Implement this method to abort the upload of a mercury of files. When this method returns,
     * none of the files forming part of the upload should be available for download.
     *
     * @param request
     * @param response
     * @param batchId
     * @return
     * @throws Exception
     */
    public abstract boolean discardFiles( HttpServletRequest request, HttpServletResponse response, Batch batch )
        throws Exception;


    /**
     * Initialize the filter. Read all configurable parameters.
     *
     * @see org.sonatype.servlet.PutFilter#init(javax.servlet.FilterConfig)
     */
    public void init( FilterConfig config )
        throws ServletException
    {
        super.init( config );

        //allow name of headers to be exchanged to be configured
        String s = config.getInitParameter( "batchIdHeader" );
        if ( s != null )
        {
            _batchIdHeader = s;
        }
        s = config.getInitParameter( "batchSupportedHeader" );
        if ( s != null )
        {
            _batchSupportedHeader = s;
        }
        s = config.getInitParameter( "batchCommitHeader" );
        if ( s != null )
        {
            _batchCommitHeader = s;
        }
        s = config.getInitParameter( "batchDiscardHeader" );
        if ( s != null )
        {
            _batchDiscardHeader = s;
        }
    }


    /**
     * Run the filter.
     *
     * @see org.sonatype.servlet.PutFilter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    public void doFilter( ServletRequest req, ServletResponse res, FilterChain chain )
        throws IOException, ServletException
    {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        //if GET fall through to filter chain
        if ( request.getMethod().equals( HttpMethods.GET ) )
        {
            chain.doFilter( req, res );
            return;
        }


        String batchId = request.getHeader( _batchIdHeader );
        String commitId = request.getHeader( _batchCommitHeader );
        String discardId = request.getHeader( _batchDiscardHeader );

        //System.err.println("method="+request.getMethod()+" batchid="+batchId+" commitId="+commitId+" discardId="+discardId);

        //we can't do  atomic batches, handle as a normal PUT
        if ( batchId == null && commitId == null && discardId == null )
        {
            System.err.println( "Not a batching request, doing PutFilter instead" );
            super.doFilter( req, res, chain );
            return;
        }

        /* TODO Is it worth handling this situation? This would mean that a directory was sent as the url 
         * along with a batchId. The cost is that the pathContext would be calculated twice in this case.
         
        if (pathInContext.endsWith("/"))
        {
            super.doFilter(req,res,chain); 
            return;
        }
        */

        if ( batchId != null )
        {
            handlePut( request, response, batchId );
            return;
        }

        if ( discardId != null )
        {
            handleDiscard( request, response, discardId );
            return;
        }

        if ( commitId != null )
        {
            handleCommit( request, response, commitId );
            return;
        }

        //otherwise - shouldn't get here
        chain.doFilter( req, res );
    }

    /**
     * Handle a PUT request.
     * <p/>
     * The batchId is saved to a list of currently active batchIds so that
     * all files forming part of the mercury can be committed or discarded as a
     * whole later on.
     * <p/>
     * If a file already exists, then status 200 is returned; if the file
     * did not previously exist, then status 201 is returned, otherwise
     * a 403 is returned.
     *
     * @param request
     * @param response
     * @param batchId
     * @throws ServletException
     * @throws IOException
     */
    public void handlePut( HttpServletRequest request, HttpServletResponse response, String batchId )
        throws ServletException, IOException
    {
        String servletPath = request.getServletPath();
        String pathInfo = request.getPathInfo();
        String pathInContext = URIUtil.addPaths( servletPath, pathInfo );
        String finalResource = URIUtil.addPaths( _baseURI, pathInContext );
        File finalFile = null;
        try
        {
            finalFile = new File( new URI( finalResource ) );
            boolean exists = finalFile.exists();

            putFile( request, response, pathInContext, batchId );

            Batch batch = addBatch( batchId, finalResource );

            String contextPath = _context.getContextPath();
            if ( contextPath.equals( "" ) )
            {
                contextPath = "/";
            }
            if ( !contextPath.endsWith( "/" ) )
            {
                contextPath += "/";
            }
            String commitBatchUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + contextPath;
            response.setHeader( _batchSupportedHeader, commitBatchUrl );
            response.setStatus( exists ? HttpServletResponse.SC_OK : HttpServletResponse.SC_CREATED );
            response.flushBuffer();
        }
        catch ( Exception ex )
        {
            _context.log( ex.toString(), ex );
            response.sendError( HttpServletResponse.SC_FORBIDDEN );
        }
    }

    /**
     * Client side wants us to discard all files in mercury.
     *
     * @param request
     * @param response
     * @param batchId
     * @throws ServletException
     * @throws IOException
     */
    public void handleDiscard( HttpServletRequest request, HttpServletResponse response, String batchId )
        throws ServletException, IOException
    {
        boolean ok = true;
        try
        {
            ok = discardFiles( request, response, _batches.get( batchId ) );
            response.setStatus( ( ok ? HttpServletResponse.SC_OK : HttpServletResponse.SC_INTERNAL_SERVER_ERROR ) );
            response.flushBuffer();
        }
        catch ( Exception ex )
        {
            _context.log( ex.toString(), ex );
            response.sendError( HttpServletResponse.SC_INTERNAL_SERVER_ERROR );
        }
        finally
        {
            updateBatch( batchId, ok );
        }
    }


    /**
     * Client side wants us to move files into final position.
     *
     * @param request
     * @param response
     * @param batchId
     * @throws ServletException
     * @throws IOException
     */
    public void handleCommit( HttpServletRequest request, HttpServletResponse response, String batchId )
        throws ServletException, IOException
    {
        boolean ok = true;
        try
        {
            ok = commitFiles( request, response, _batches.get( batchId ) );
            response.setStatus( ( ok ? HttpServletResponse.SC_OK : HttpServletResponse.SC_INTERNAL_SERVER_ERROR ) );
            response.flushBuffer();
        }
        catch ( Exception ex )
        {
            _context.log( ex.toString(), ex );
            response.sendError( HttpServletResponse.SC_INTERNAL_SERVER_ERROR );
        }
        finally
        {
            updateBatch( batchId, ok );
        }
    }

    /**
     * Default behaviour is to put the file directly to it's final location.
     * <p/>
     * Subclasses can choose to override this method and put the file
     * into a staging area first.
     *
     * @param request
     * @param response
     * @param pathInContext
     * @param batchId
     * @throws Exception
     * @see org.sonatype.mercury.server.jetty.DefaultBatchFilter
     */
    public void putFile( HttpServletRequest request,
                         HttpServletResponse response,
                         String pathInContext,
                         String batchId )
        throws Exception
    {
        String finalResource = URIUtil.addPaths( _baseURI, pathInContext );
        File finalFile = null;
        finalFile = new File( new URI( finalResource ) );
        File parent = finalFile.getParentFile();
        parent.mkdirs();
        int toRead = request.getContentLength();
        InputStream in = request.getInputStream();
        OutputStream out = new FileOutputStream( finalFile, false );
        if ( toRead >= 0 )
        {
            IO.copy( in, out, toRead );
        }
        else
        {
            IO.copy( in, out );
        }

    }

    /**
     * Remember a mercury, or update the count of files in the mercury.
     *
     * @param batchId
     */
    protected Batch addBatch( String batchId, String file )
    {
        Batch status = (Batch) _batches.get( batchId );
        long timestamp = System.currentTimeMillis();
        if ( status == null )
        {
            status = new Batch( batchId, timestamp );
            _batches.put( batchId, status );
        }
        status.addFile( file );
        return status;
    }


    /**
     * Update the status of the mercury.
     *
     * @param batchId
     * @param ok      if true, the mercury job is removed from the list; otherwise it is marked as failed
     */
    protected void updateBatch( String batchId, boolean ok )
    {
        Batch status = (Batch) _batches.get( batchId );
        if ( status == null )
        {
            _context.log( "Unknown mercury id to update: " + batchId );
        }
        else
        {
            if ( ok )
            {
                _batches.remove( batchId );
            }
            else
            {
                status.failed(); //mark as failed
            }
        }
    }
}
