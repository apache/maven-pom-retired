/*
 * $Id$
 */

package org.apache.maven.mercury.repository.metadata;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import java.util.Date;

/**
 * null
 * 
 * @version $Revision$ $Date$
 */
public class Metadata implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field groupId
     */
    private String groupId;

    /**
     * Field artifactId
     */
    private String artifactId;

    /**
     * Field version
     */
    private String version;

    /**
     * Field versioning
     */
    private Versioning versioning;

    /**
     * Field plugins
     */
    private java.util.List plugins;


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Method addPlugin
     * 
     * @param plugin
     */
    public void addPlugin(Plugin plugin)
    {
        if ( !(plugin instanceof Plugin) )
        {
            throw new ClassCastException( "Metadata.addPlugins(plugin) parameter must be instanceof " + Plugin.class.getName() );
        }
        getPlugins().add( plugin );
    } //-- void addPlugin(Plugin) 

    /**
     * Get The artifactId that is directory represents, if any.
     */
    public String getArtifactId()
    {
        return this.artifactId;
    } //-- String getArtifactId() 

    /**
     * Get The groupId that is directory represents, if any.
     */
    public String getGroupId()
    {
        return this.groupId;
    } //-- String getGroupId() 

    /**
     * Method getPlugins
     */
    public java.util.List getPlugins()
    {
        if ( this.plugins == null )
        {
            this.plugins = new java.util.ArrayList();
        }
        
        return this.plugins;
    } //-- java.util.List getPlugins() 

    /**
     * Get The version that is directory represents, if any.
     */
    public String getVersion()
    {
        return this.version;
    } //-- String getVersion() 

    /**
     * Get Versioning information for the artifact.
     */
    public Versioning getVersioning()
    {
        return this.versioning;
    } //-- Versioning getVersioning() 

    /**
     * Method removePlugin
     * 
     * @param plugin
     */
    public void removePlugin(Plugin plugin)
    {
        if ( !(plugin instanceof Plugin) )
        {
            throw new ClassCastException( "Metadata.removePlugins(plugin) parameter must be instanceof " + Plugin.class.getName() );
        }
        getPlugins().remove( plugin );
    } //-- void removePlugin(Plugin) 

    /**
     * Set The artifactId that is directory represents, if any.
     * 
     * @param artifactId
     */
    public void setArtifactId(String artifactId)
    {
        this.artifactId = artifactId;
    } //-- void setArtifactId(String) 

    /**
     * Set The groupId that is directory represents, if any.
     * 
     * @param groupId
     */
    public void setGroupId(String groupId)
    {
        this.groupId = groupId;
    } //-- void setGroupId(String) 

    /**
     * Set The set of plugin mappings for the group
     * 
     * @param plugins
     */
    public void setPlugins(java.util.List plugins)
    {
        this.plugins = plugins;
    } //-- void setPlugins(java.util.List) 

    /**
     * Set The version that is directory represents, if any.
     * 
     * @param version
     */
    public void setVersion(String version)
    {
        this.version = version;
    } //-- void setVersion(String) 

    /**
     * Set Versioning information for the artifact.
     * 
     * @param versioning
     */
    public void setVersioning(Versioning versioning)
    {
        this.versioning = versioning;
    } //-- void setVersioning(Versioning) 


    public boolean merge( Metadata sourceMetadata )
    {
        boolean changed = false;

        for ( java.util.Iterator i = sourceMetadata.getPlugins().iterator(); i.hasNext(); )
        {
            Plugin plugin = (Plugin) i.next();
            boolean found = false;

            for ( java.util.Iterator it = getPlugins().iterator(); it.hasNext() && !found; )
            {
                Plugin preExisting = (Plugin) it.next();

                if ( preExisting.getPrefix().equals( plugin.getPrefix() ) )
                {
                    found = true;
                }
            }

            if ( !found )
            {
                Plugin mappedPlugin = new Plugin();

                mappedPlugin.setArtifactId( plugin.getArtifactId() );

                mappedPlugin.setPrefix( plugin.getPrefix() );

                mappedPlugin.setName( plugin.getName() );

                addPlugin( mappedPlugin );

                changed = true;
            }
        }

        Versioning versioning = sourceMetadata.getVersioning();
        if ( versioning != null )
        {
            Versioning v = getVersioning();
            if ( v == null )
            {
                v = new Versioning();
                setVersioning( v );
                changed = true;
            }

            for ( java.util.Iterator i = versioning.getVersions().iterator(); i.hasNext(); )
            {
                String version = (String) i.next();
                if ( !v.getVersions().contains( version ) )
                {
                    changed = true;
                    v.getVersions().add( version );
                }
            }
          
            if ( "null".equals( versioning.getLastUpdated() ) )
            {
                versioning.setLastUpdated( null );
            }

            if ( "null".equals( v.getLastUpdated() ) )
            {
                v.setLastUpdated( null );
            }

            if ( versioning.getLastUpdated() == null || versioning.getLastUpdated().length() == 0 )
            {
                // this should only be for historical reasons - we assume local is newer
                versioning.setLastUpdated( v.getLastUpdated() );
            }

            if ( v.getLastUpdated() == null || v.getLastUpdated().length() == 0 ||
                 versioning.getLastUpdated().compareTo( v.getLastUpdated() ) >= 0 )
            {
                changed = true;
                v.setLastUpdated( versioning.getLastUpdated() );

                if ( versioning.getRelease() != null )
                {
                    changed = true;
                    v.setRelease( versioning.getRelease() );
                }
                if ( versioning.getLatest() != null )
                {
                    changed = true;
                    v.setLatest( versioning.getLatest() );
                }

                Snapshot s = v.getSnapshot();
                Snapshot snapshot = versioning.getSnapshot();
                if ( snapshot != null )
                {
                    if ( s == null )
                    {
                        s = new Snapshot();
                        v.setSnapshot( s );
                        changed = true;
                    }

                    // overwrite
                    if ( s.getTimestamp() == null ? snapshot.getTimestamp() != null
                        : !s.getTimestamp().equals( snapshot.getTimestamp() ) )
                    {
                        s.setTimestamp( snapshot.getTimestamp() );
                        changed = true;
                    }
                    if ( s.getBuildNumber() != snapshot.getBuildNumber() )
                    {
                        s.setBuildNumber( snapshot.getBuildNumber() );
                        changed = true;
                    }
                    if ( s.isLocalCopy() != snapshot.isLocalCopy() )
                    {
                        s.setLocalCopy( snapshot.isLocalCopy() );
                        changed = true;
                    }
                }
            }
        }
        return changed;
    }
          
    private String modelEncoding = "UTF-8";

    public void setModelEncoding( String modelEncoding )
    {
        this.modelEncoding = modelEncoding;
    }

    public String getModelEncoding()
    {
        return modelEncoding;
    }
}
