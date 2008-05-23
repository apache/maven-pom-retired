// ========================================================================
// Copyright 2008 Sonatype Inc.
// ------------------------------------------------------------------------
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
// http://www.apache.org/licenses/LICENSE-2.0
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// ========================================================================
package org.apache.maven.mercury.client.retrieve;

import org.apache.maven.mercury.client.BatchException;
import org.apache.maven.mercury.client.Binding;
import org.apache.maven.mercury.client.FileExchange;
import org.apache.maven.mercury.validate.Validator;
import org.mortbay.jetty.client.HttpExchange;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;


/**
 * RetrievalTarget
 * <p/>
 * A RetrievalTarget is a remote file that must be downloaded locally, checksummed
 * and then atomically moved to its final location. The RetrievalTarget encapsulates
 * the temporary local file to which the remote file is downloaded, and also the
 * retrieval of the checksum file and the checksum calculation.
 */
public abstract class RetrievalTarget
{
    public static final String __PREFIX = "JTY_";
    public static final String __DIGEST_SUFFIX = ".sha1";
    public static final String __TEMP_SUFFIX = ".tmp";
    public static final int __START_STATE = 1;
    public static final int __REQUESTED_STATE = 2;
    public static final int __READY_STATE = 3;

    private int _checksumState;
    private int _targetState;

    private BatchException _exception;
    private Binding _binding;
    private File _tempFile;
    private String _checksumUrl;
    private String _retrievedChecksum;
    private String _calculatedChecksum;
    private DefaultRetriever _retriever;
    private boolean _complete;
    private HttpExchange _exchange;
    private Set<Validator> _validators;

    public abstract void onComplete();

    public abstract void onError( BatchException exception );


    /**
     * Constructor
     *
     * @param binding
     * @param callback
     */
    public RetrievalTarget( DefaultRetriever retriever, Binding binding, Set<Validator> validators )
    {
        if ( binding == null || binding.getRemoteUrl() == null || binding.getLocalFile() == null )
        {
            throw new IllegalArgumentException( "No file to retrieve" );
        }
        _retriever = retriever;
        _binding = binding;
        _validators = validators;
        _checksumUrl = _binding.getRemoteUrl() + __DIGEST_SUFFIX;
        _tempFile = new File( _binding.getLocalFile().getParentFile(),
            __PREFIX + _binding.getLocalFile().getName() + __TEMP_SUFFIX );
        _tempFile.deleteOnExit();
        if ( _tempFile.exists() )
        {
            onError( new BatchException( binding, "File exists " + _tempFile.getAbsolutePath() ) );
        }
        else if ( !_tempFile.getParentFile().canWrite() )
        {
            onError( new BatchException( binding,
                "Unable to write to dir " + _tempFile.getParentFile().getAbsolutePath() ) );
        }
    }

    public boolean isLenientChecksum()
    {
        return _binding.isLenientChecksum();
    }

    public void setRetrievedChecksum( String retrievedChecksum )
    {
        _retrievedChecksum = retrievedChecksum;
    }

    public String getExpectedChecksum()
    {
        return _retrievedChecksum;
    }

    public void setCalculatedChecksum( String calculatedChecksum )
    {
        _calculatedChecksum = calculatedChecksum;
    }

    public String getActualChecksum()
    {
        return _calculatedChecksum;
    }

    public File getTempFile()
    {
        return _tempFile;
    }

    public String getUrl()
    {
        return _binding.getRemoteUrl();
    }


    /** Retrieve the remote file and its checksum file using jetty async httpclient. */
    public void retrieve()
    {
        updateChecksumState( __START_STATE, null );
        updateTargetState( __START_STATE, null );
    }


    /** Move the temporary file to its final location */
    public boolean move()
    {
        return _tempFile.renameTo( _binding.getLocalFile() );
    }

    /** Cleanup temp files */
    public synchronized void cleanup()
    {
        deleteTempFile();
        if ( _exchange != null )
        {
            _exchange.cancel();
        }
    }


    /**
     * Check the actual checksum against the expected checksum
     *
     * @return
     */
    public boolean verifyChecksum()
    {
        if ( _retrievedChecksum != null && _calculatedChecksum != null && _calculatedChecksum.equals(
            _retrievedChecksum ) )
        {
            return true;
        }
        else if ( _retrievedChecksum == null && _binding.isLenientChecksum() )
        {
            return true;
        }

        return false;
    }

    public boolean validate( List<String> errors )
    {
        if ( _validators == null || _validators.isEmpty() )
        {
            return true;
        }

        String ext = ( _binding.getLocalFile() == null ? null : _binding.getLocalFile().getName() );
        int i = ext.lastIndexOf( "." );
        ext = ( i > 0 ? ext.substring( i + 1 ) : "" );

        for ( Validator v : _validators )
        {
            String vExt = v.getFileExtension();
            if ( vExt.equalsIgnoreCase( ext ) )
            {
                try
                {
                    if ( !v.validate( _tempFile.getCanonicalPath(), errors ) )
                    {
                        return false;
                    }
                }
                catch ( IOException e )
                {
                    errors.add( e.getMessage() );
                    return false;
                }
            }
        }
        return true;
    }

    protected synchronized void updateChecksumState( int state, Throwable ex )
    {
        _checksumState = state;
        if ( _exception == null && ex != null )
        {
            if ( ex instanceof BatchException )
            {
                _exception = (BatchException) ex;
            }
            else
            {
                _exception = new BatchException( _binding, ex );
            }
        }

        if ( _checksumState == __START_STATE )
        {
            _exchange = retrieveChecksum();
        }

        //if both checksum and target file are ready, we're ready to return callback
        if ( _checksumState == __READY_STATE && _targetState == __READY_STATE )
        {
            _complete = true;
            if ( _exception == null )
            {
                onComplete();
            }
            else
            {
                onError( _exception );
            }
        }
    }

    protected synchronized void updateTargetState( int state, Throwable ex )
    {
        _targetState = state;
        if ( _exception == null && ex != null )
        {
            if ( ex instanceof BatchException )
            {
                _exception = (BatchException) ex;
            }
            else
            {
                _exception = new BatchException( _binding, ex );
            }
        }

        if ( _targetState == __START_STATE )
        {
            _exchange = retrieveTargetFile();
        }

        //if both checksum and target file are ready, we're ready to return callback
        if ( _checksumState == __READY_STATE && _targetState == __READY_STATE )
        {
            _complete = true;
            if ( _exception == null )
            {
                onComplete();
            }
            else
            {
                onError( _exception );
            }
        }
    }

    /** Asynchronously fetch the checksum for the target file. */
    private HttpExchange retrieveChecksum()
    {
        updateChecksumState( __REQUESTED_STATE, null );
        HttpExchange exchange = new HttpExchange.ContentExchange()
        {
            protected void onException( Throwable ex )
            {
                updateChecksumState( __READY_STATE, ex );
            }

            protected void onResponseComplete() throws IOException
            {
                super.onResponseComplete();
                if ( getResponseStatus() == HttpServletResponse.SC_OK )
                {
                    setRetrievedChecksum( getResponseContent().trim() );
                }

                updateChecksumState( __READY_STATE, null );
            }
        };
        exchange.setURL( _checksumUrl );

        try
        {
            _retriever.getHttpClient().send( exchange );
        }
        catch ( IOException ex )
        {
            updateChecksumState( __READY_STATE, ex );
        }
        return exchange;
    }


    /** Asynchronously fetch the target file. */
    private HttpExchange retrieveTargetFile()
    {
        updateTargetState( __REQUESTED_STATE, null );

        //get the file, calculating the digest for it on the fly
        FileExchange exchange = new FileGetExchange( _binding, getTempFile(), true, _retriever.getHttpClient() )
        {
            public void onFileComplete( String url, File localFile, String digest )
            {
                //we got the target file ok, so tell our main callback
                _targetState = __READY_STATE;
                setCalculatedChecksum( digest );
                updateTargetState( __READY_STATE, null );
            }

            public void onFileError( String url, Exception e )
            {
                //an error occurred whilst fetching the file, return an error
                _targetState = __READY_STATE;
                updateTargetState( __READY_STATE, e );
            }
        };
        exchange.send();
        return exchange;
    }


    private boolean deleteTempFile()
    {
        if ( _tempFile != null && _tempFile.exists() )
        {
            return _tempFile.delete();
        }
        return false;
    }

    public synchronized boolean isComplete()
    {
        return _complete;
    }

    public String toString()
    {
        return "T:" + _binding.getRemoteUrl() + ":" + _targetState + ":" + _checksumState + ":" + _complete;
    }

    public String getRetrievedChecksum()
    {
        return _retrievedChecksum;
    }

    public String getCalculatedChecksum()
    {
        return _calculatedChecksum;
    }
}
