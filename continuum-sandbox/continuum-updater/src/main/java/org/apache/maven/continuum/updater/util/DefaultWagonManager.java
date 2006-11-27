package org.apache.maven.continuum.updater.util;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
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

import org.apache.maven.wagon.ConnectionException;
import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.apache.maven.wagon.TransferFailedException;
import org.apache.maven.wagon.UnsupportedProtocolException;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.authentication.AuthenticationException;
import org.apache.maven.wagon.authentication.AuthenticationInfo;
import org.apache.maven.wagon.authorization.AuthorizationException;
import org.apache.maven.wagon.events.TransferListener;
import org.apache.maven.wagon.observers.ChecksumObserver;
import org.apache.maven.wagon.providers.http.LightweightHttpWagon;
import org.apache.maven.wagon.providers.ftp.FtpWagon;
import org.apache.maven.wagon.proxy.ProxyInfo;
import org.apache.maven.wagon.repository.Repository;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLifecycleException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.net.URL;

/**
 * @plexus.component
 *   role="org.apache.maven.continuum.updater.util.WagonManager"
 *
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class DefaultWagonManager
    extends AbstractLogEnabled
    implements WagonManager
{
    public static final String CHECKSUM_POLICY_FAIL = "fail";

    public static final String CHECKSUM_POLICY_IGNORE = "ignore";

    private PlexusContainer container;

    private TransferListener downloadMonitor;

    public Wagon getWagon( String protocol )
        throws UnsupportedProtocolException
    {
        Wagon wagon = null;

/*
        try
        {
            wagon = (Wagon) embedder.lookup( Wagon.ROLE, protocol );
        }
        catch ( ComponentLookupException e )
        {
            throw new UnsupportedProtocolException(
                "Cannot find wagon which supports the requested protocol: " + protocol, e );
        }
*/
        if ( "http".equals( protocol ) )
        {
            wagon = new LightweightHttpWagon();
        }
        else if ( "ftp".equals( protocol ) )
        {
            wagon = new FtpWagon();
        }

        return wagon;
    }

    public void getFile( URL url, File destination, String checksumPolicy )
        throws TransferFailedException, ResourceDoesNotExistException, ChecksumFailedException
    {
        // TODO: better excetpions - transfer failed is not enough?

        Wagon wagon;

        String protocol = url.getProtocol();

        String remotePath = url.getPath();
        try
        {
            wagon = getWagon( protocol );
        }
        catch ( UnsupportedProtocolException e )
        {
            throw new TransferFailedException( "Unsupported Protocol: ", e );
        }

        if ( downloadMonitor != null )
        {
            wagon.addTransferListener( downloadMonitor );
        }

        ChecksumObserver md5ChecksumObserver;

        ChecksumObserver sha1ChecksumObserver;

        try
        {
            md5ChecksumObserver = new ChecksumObserver( "MD5" );

            wagon.addTransferListener( md5ChecksumObserver );

            sha1ChecksumObserver = new ChecksumObserver( "SHA-1" );

            wagon.addTransferListener( sha1ChecksumObserver );
        }
        catch ( NoSuchAlgorithmException e )
        {
            throw new TransferFailedException( "Unable to add checksum methods", e );
        }

        File temp = new File( destination + ".tmp" );

        temp.deleteOnExit();

        try
        {
            wagon.connect( new Repository( "continuum", protocol + "://" + url.getHost() ), null, null );

            boolean firstRun = true;

            boolean retry = true;

            // this will run at most twice. The first time, the firstRun flag is turned off, and if the retry flag
            // is set on the first run, it will be turned off and not re-set on the second try. This is because the
            // only way the retry flag can be set is if ( firstRun == true ).
            while ( firstRun || retry )
            {
                // reset the retry flag.
                retry = false;

                // This should take care of creating destination directory now on
                wagon.get( remotePath, temp );

                // keep the checksum files from showing up on the download monitor...
                if ( downloadMonitor != null )
                {
                    wagon.removeTransferListener( downloadMonitor );
                }

                // try to verify the SHA-1 checksum for this file.
                try
                {
                    verifyChecksum( sha1ChecksumObserver, temp, remotePath, ".sha1", wagon );
                }
                catch ( ChecksumFailedException e )
                {
                    // if we catch a ChecksumFailedException, it means the transfer/read succeeded, but the checksum
                    // doesn't match. This could be a problem with the server (ibiblio HTTP-200 error page), so we'll
                    // try this up to two times. On the second try, we'll handle it as a bona-fide error, based on the
                    // repository's checksum checking policy.
                    if ( firstRun )
                    {
                        getLogger().warn( "*** CHECKSUM FAILED - " + e.getMessage() + " - RETRYING" );
                        retry = true;
                    }
                    else
                    {
                        handleChecksumFailure( checksumPolicy, e.getMessage(), e.getCause() );
                    }
                }
                catch ( ResourceDoesNotExistException sha1TryException )
                {
                    getLogger().debug( "SHA1 not found, trying MD5", sha1TryException );

                    // if this IS NOT a ChecksumFailedException, it was a problem with transfer/read of the checksum
                    // file...we'll try again with the MD5 checksum.
                    try
                    {
                        verifyChecksum( md5ChecksumObserver, temp, remotePath, ".md5", wagon );
                    }
                    catch ( ChecksumFailedException e )
                    {
                        // if we also fail to verify based on the MD5 checksum, and the checksum transfer/read
                        // succeeded, then we need to determine whether to retry or handle it as a failure.
                        if ( firstRun )
                        {
                            retry = true;
                        }
                        else
                        {
                            handleChecksumFailure( checksumPolicy, e.getMessage(), e.getCause() );
                        }
                    }
                    catch ( ResourceDoesNotExistException md5TryException )
                    {
                        // this was a failed transfer, and we don't want to retry.
                        handleChecksumFailure( checksumPolicy, "Error retrieving checksum file for " + remotePath,
                                               md5TryException );
                    }
                }

                // reinstate the download monitor...
                if ( downloadMonitor != null )
                {
                    wagon.addTransferListener( downloadMonitor );
                }

                // unset the firstRun flag, so we don't get caught in an infinite loop...
                firstRun = false;
            }
        }
        catch ( ConnectionException e )
        {
            throw new TransferFailedException( "Connection failed: ", e );
        }
        catch ( AuthenticationException e )
        {
            throw new TransferFailedException( "Authentication failed: ", e );
        }
        catch ( AuthorizationException e )
        {
            throw new TransferFailedException( "Authorization failed: ", e );
        }
        finally
        {
            disconnectWagon( wagon );

            releaseWagon( wagon );
        }

        if ( !temp.exists() )
        {
            throw new ResourceDoesNotExistException( "Downloaded file does not exist: " + temp );
        }

        // The temporary file is named destination + ".tmp" and is done this way to ensure
        // that the temporary file is in the same file system as the destination because the
        // File.renameTo operation doesn't really work across file systems.
        // So we will attempt to do a File.renameTo for efficiency and atomicity, if this fails
        // then we will use a brute force copy and delete the temporary file.

        if ( !temp.renameTo( destination ) )
        {
            try
            {
                FileUtils.copyFile( temp, destination );

                temp.delete();
            }
            catch ( IOException e )
            {
                throw new TransferFailedException( "Error copying temporary file to the final destination: ", e );
            }
        }
    }

    private void handleChecksumFailure( String checksumPolicy, String message, Throwable cause )
        throws ChecksumFailedException
    {
        if ( CHECKSUM_POLICY_FAIL.equals( checksumPolicy ) )
        {
            throw new ChecksumFailedException( message, cause );
        }
        else if ( !CHECKSUM_POLICY_IGNORE.equals( checksumPolicy ) )
        {
            // warn if it is set to anything other than ignore
            getLogger().warn( "*** CHECKSUM FAILED - " + message + " - IGNORING" );
        }
        // otherwise it is ignore
    }

    private void verifyChecksum( ChecksumObserver checksumObserver, File destination, String remotePath,
                                 String checksumFileExtension, Wagon wagon )
        throws ResourceDoesNotExistException, TransferFailedException, AuthorizationException
    {
        try
        {
            // grab it first, because it's about to change...
            String actualChecksum = checksumObserver.getActualChecksum();

            File checksumFile = new File( destination + checksumFileExtension );

            checksumFile.deleteOnExit();

            wagon.get( remotePath + checksumFileExtension, checksumFile );

            String expectedChecksum = FileUtils.fileRead( checksumFile );

            // remove whitespaces at the end
            expectedChecksum = expectedChecksum.trim();

            // check for 'MD5 (name) = CHECKSUM'
            if ( expectedChecksum.startsWith( "MD5" ) )
            {
                int lastSpacePos = expectedChecksum.lastIndexOf( ' ' );

                expectedChecksum = expectedChecksum.substring( lastSpacePos + 1 );
            }
            else
            {
                // remove everything after the first space (if available)
                int spacePos = expectedChecksum.indexOf( ' ' );

                if ( spacePos != -1 )
                {
                    expectedChecksum = expectedChecksum.substring( 0, spacePos );
                }
            }
            if ( !expectedChecksum.equals( actualChecksum ) )
            {
                throw new ChecksumFailedException( "Checksum failed on download: local = '" + actualChecksum +
                    "'; remote = '" + expectedChecksum + "'" );
            }
        }
        catch ( IOException e )
        {
            throw new TransferFailedException( "Invalid checksum file", e );
        }
    }

    private void disconnectWagon( Wagon wagon )
    {
        try
        {
            wagon.disconnect();
        }
        catch ( ConnectionException e )
        {
            getLogger().error( "Problem disconnecting from wagon - ignoring: " + e.getMessage() );
        }
    }

    private void releaseWagon( Wagon wagon )
    {
/*
        try
        {
            container.release( wagon );
        }
        catch ( ComponentLifecycleException e )
        {
            getLogger().error( "Problem releasing wagon - ignoring: " + e.getMessage() );
        }
*/
    }

    public void contextualize( Context context )
        throws ContextException
    {
        container = (PlexusContainer) context.get( PlexusConstants.PLEXUS_KEY );
    }
}
