/*
 * $Id$
 */

package org.apache.maven.mercury.repository.metadata.io.xpp3;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import java.io.Writer;
import java.text.DateFormat;
import java.util.Iterator;
import java.util.Locale;
import org.apache.maven.mercury.repository.metadata.Metadata;
import org.apache.maven.mercury.repository.metadata.Plugin;
import org.apache.maven.mercury.repository.metadata.Snapshot;
import org.apache.maven.mercury.repository.metadata.Versioning;
import org.codehaus.plexus.util.xml.pull.MXSerializer;
import org.codehaus.plexus.util.xml.pull.XmlSerializer;

/**
 * Class MetadataXpp3Writer.
 * 
 * @version $Revision$ $Date$
 */
public class MetadataXpp3Writer {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field NAMESPACE
     */
    private String NAMESPACE;


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Method write
     * 
     * @param writer
     * @param metadata
     */
    public void write(Writer writer, Metadata metadata)
        throws java.io.IOException
    {
        XmlSerializer serializer = new MXSerializer();
        serializer.setProperty( "http://xmlpull.org/v1/doc/properties.html#serializer-indentation", "  " );
        serializer.setProperty( "http://xmlpull.org/v1/doc/properties.html#serializer-line-separator", "\n" );
        serializer.setOutput( writer );
        serializer.startDocument( metadata.getModelEncoding(), null );
        writeMetadata( metadata, "metadata", serializer );
        serializer.endDocument();
    } //-- void write(Writer, Metadata) 

    /**
     * Method writeMetadata
     * 
     * @param metadata
     * @param serializer
     * @param tagName
     */
    private void writeMetadata(Metadata metadata, String tagName, XmlSerializer serializer)
        throws java.io.IOException
    {
        if ( metadata != null )
        {
            serializer.startTag( NAMESPACE, tagName );
            if ( metadata.getGroupId() != null )
            {
                serializer.startTag( NAMESPACE, "groupId" ).text( metadata.getGroupId() ).endTag( NAMESPACE, "groupId" );
            }
            if ( metadata.getArtifactId() != null )
            {
                serializer.startTag( NAMESPACE, "artifactId" ).text( metadata.getArtifactId() ).endTag( NAMESPACE, "artifactId" );
            }
            if ( metadata.getVersion() != null )
            {
                serializer.startTag( NAMESPACE, "version" ).text( metadata.getVersion() ).endTag( NAMESPACE, "version" );
            }
            if ( metadata.getVersioning() != null )
            {
                writeVersioning( (Versioning) metadata.getVersioning(), "versioning", serializer );
            }
            if ( metadata.getPlugins() != null && metadata.getPlugins().size() > 0 )
            {
                serializer.startTag( NAMESPACE, "plugins" );
                for ( Iterator iter = metadata.getPlugins().iterator(); iter.hasNext(); )
                {
                    Plugin o = (Plugin) iter.next();
                    writePlugin( o, "plugin", serializer );
                }
                serializer.endTag( NAMESPACE, "plugins" );
            }
            serializer.endTag( NAMESPACE, tagName );
        }
    } //-- void writeMetadata(Metadata, String, XmlSerializer) 

    /**
     * Method writePlugin
     * 
     * @param plugin
     * @param serializer
     * @param tagName
     */
    private void writePlugin(Plugin plugin, String tagName, XmlSerializer serializer)
        throws java.io.IOException
    {
        if ( plugin != null )
        {
            serializer.startTag( NAMESPACE, tagName );
            if ( plugin.getName() != null )
            {
                serializer.startTag( NAMESPACE, "name" ).text( plugin.getName() ).endTag( NAMESPACE, "name" );
            }
            if ( plugin.getPrefix() != null )
            {
                serializer.startTag( NAMESPACE, "prefix" ).text( plugin.getPrefix() ).endTag( NAMESPACE, "prefix" );
            }
            if ( plugin.getArtifactId() != null )
            {
                serializer.startTag( NAMESPACE, "artifactId" ).text( plugin.getArtifactId() ).endTag( NAMESPACE, "artifactId" );
            }
            serializer.endTag( NAMESPACE, tagName );
        }
    } //-- void writePlugin(Plugin, String, XmlSerializer) 

    /**
     * Method writeSnapshot
     * 
     * @param snapshot
     * @param serializer
     * @param tagName
     */
    private void writeSnapshot(Snapshot snapshot, String tagName, XmlSerializer serializer)
        throws java.io.IOException
    {
        if ( snapshot != null )
        {
            serializer.startTag( NAMESPACE, tagName );
            if ( snapshot.getTimestamp() != null )
            {
                serializer.startTag( NAMESPACE, "timestamp" ).text( snapshot.getTimestamp() ).endTag( NAMESPACE, "timestamp" );
            }
            if ( snapshot.getBuildNumber() != 0 )
            {
                serializer.startTag( NAMESPACE, "buildNumber" ).text( String.valueOf( snapshot.getBuildNumber() ) ).endTag( NAMESPACE, "buildNumber" );
            }
            if ( snapshot.isLocalCopy() != false )
            {
                serializer.startTag( NAMESPACE, "localCopy" ).text( String.valueOf( snapshot.isLocalCopy() ) ).endTag( NAMESPACE, "localCopy" );
            }
            serializer.endTag( NAMESPACE, tagName );
        }
    } //-- void writeSnapshot(Snapshot, String, XmlSerializer) 

    /**
     * Method writeVersioning
     * 
     * @param versioning
     * @param serializer
     * @param tagName
     */
    private void writeVersioning(Versioning versioning, String tagName, XmlSerializer serializer)
        throws java.io.IOException
    {
        if ( versioning != null )
        {
            serializer.startTag( NAMESPACE, tagName );
            if ( versioning.getLatest() != null )
            {
                serializer.startTag( NAMESPACE, "latest" ).text( versioning.getLatest() ).endTag( NAMESPACE, "latest" );
            }
            if ( versioning.getRelease() != null )
            {
                serializer.startTag( NAMESPACE, "release" ).text( versioning.getRelease() ).endTag( NAMESPACE, "release" );
            }
            if ( versioning.getSnapshot() != null )
            {
                writeSnapshot( (Snapshot) versioning.getSnapshot(), "snapshot", serializer );
            }
            if ( versioning.getVersions() != null && versioning.getVersions().size() > 0 )
            {
                serializer.startTag( NAMESPACE, "versions" );
                for ( Iterator iter = versioning.getVersions().iterator(); iter.hasNext(); )
                {
                    String version = (String) iter.next();
                    serializer.startTag( NAMESPACE, "version" ).text( version ).endTag( NAMESPACE, "version" );
                }
                serializer.endTag( NAMESPACE, "versions" );
            }
            if ( versioning.getLastUpdated() != null )
            {
                serializer.startTag( NAMESPACE, "lastUpdated" ).text( versioning.getLastUpdated() ).endTag( NAMESPACE, "lastUpdated" );
            }
            serializer.endTag( NAMESPACE, tagName );
        }
    } //-- void writeVersioning(Versioning, String, XmlSerializer) 


}
