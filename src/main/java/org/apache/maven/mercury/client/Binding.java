package org.apache.maven.mercury.client;

import java.io.File;

/**
 * Binding
 * <p/>
 * A Binding represents a remote url whose contents are to be downloaded
 * and stored in a local file, or a local file whose contents are to be
 * uploaded to the remote url.
 */
public class Binding
{
    protected String remoteUrl;
    protected File localFile;
    protected boolean lenientChecksum;

    public String getRemoteUrl()
    {
        return remoteUrl;
    }

    public void setRemoteUrl( String remoteUrl )
    {
        this.remoteUrl = remoteUrl;
    }

    public File getLocalFile()
    {
        return localFile;
    }

    public void setLocalFile( File localFile )
    {
        this.localFile = localFile;
    }

    public boolean isLenientChecksum()
    {
        return lenientChecksum;
    }

    public void setLenientChecksum( boolean leniantChecksum )
    {
        this.lenientChecksum = leniantChecksum;
    }

    public String toString()
    {
        return "[" + remoteUrl + "," + localFile + "," + lenientChecksum + "]";
    }
}
